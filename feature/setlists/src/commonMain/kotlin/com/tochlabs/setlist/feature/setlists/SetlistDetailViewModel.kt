package com.tochlabs.setlist.feature.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.setlist.DurationSummary
import com.tochlabs.setlist.core.domain.setlist.ItemMarkers
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.domain.setlist.calculateDuration
import com.tochlabs.setlist.core.domain.setlist.computeMarkers
import com.tochlabs.setlist.core.domain.setlist.moved
import com.tochlabs.setlist.core.domain.validation.PauseRules
import com.tochlabs.setlist.core.domain.validation.decreasePause
import com.tochlabs.setlist.core.domain.validation.increasePause
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Which figure the total card shows (specs/setlist-duration, "Toggle between totals and breakdown"). */
enum class TotalMode { WITH_STOPS, MUSIC_ONLY }

/** One item of the setlist as the detail lists it. */
data class ItemRow(
    val entry: ResolvedItem,
    /** Position among the songs (1, 2, ...); null for stops. */
    val songNumber: Int?,
    /** Null for stops. */
    val markers: ItemMarkers?,
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
    val nextIsSong: Boolean,
) {
    val id: SetlistItemId
        get() =
            when (entry) {
                is ResolvedItem.SongEntry -> entry.item.id
                is ResolvedItem.StopEntry -> entry.item.id
            }

    fun songDraft(): SongItemDraft? =
        (entry as? ResolvedItem.SongEntry)?.let { SongItemDraft(it.item, it.song, nextIsSong) }

    fun stopDraft(): StopDraft? = (entry as? ResolvedItem.StopEntry)?.let { StopDraft.of(it.item) }
}

data class SetlistDetailUiState(
    val loading: Boolean = true,
    val setlist: Setlist? = null,
    val band: Band? = null,
    /** Event linked to the setlist, if any. */
    val event: Event? = null,
    val rows: List<ItemRow> = emptyList(),
    val duration: DurationSummary = calculateDuration(emptyList(), 0),
    val totalMode: TotalMode = TotalMode.WITH_STOPS,
    /** Shows drag handles and move buttons. */
    val reorderMode: Boolean = false,
    val deleted: Boolean = false,
) {
    val shownTotalSeconds: Long
        get() =
            when (totalMode) {
                TotalMode.WITH_STOPS -> duration.totalWithStopsSeconds
                TotalMode.MUSIC_ONLY -> duration.musicSeconds
            }

    val songCount: Int get() = rows.count { it.songNumber != null }

    val canDecreasePause: Boolean get() = (setlist?.pauseSeconds ?: 0) > PauseRules.MIN_SECONDS

    val canIncreasePause: Boolean get() = (setlist?.pauseSeconds ?: PauseRules.MAX_SECONDS) < PauseRules.MAX_SECONDS
}

/** Setlist detail: items, totals, pause, reorder, item edits and deletion (specs/setlists, specs/setlist-duration). */
class SetlistDetailViewModel(
    private val setlistId: SetlistId,
    private val setlists: SetlistRepository,
    songs: SongRepository,
    bands: BandRepository,
    events: EventRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    /** Order shown while dragging, before it is saved. */
    private val pendingOrder = MutableStateFlow<List<SetlistItemId>?>(null)
    private val totalMode = MutableStateFlow(TotalMode.WITH_STOPS)
    private val reorderMode = MutableStateFlow(false)
    private val deleted = MutableStateFlow(false)

    private val content =
        combine(
            setlists.observe(setlistId),
            combine(setlists.observeItems(setlistId), pendingOrder, ::inOrder),
            songs.observeSongs(),
            bands.observeActive(),
            events.observeBySetlist(setlistId),
        ) { setlist, items, catalogue, active, event ->
            val resolved = resolveItems(items, catalogue.associateBy { it.id })
            SetlistDetailUiState(
                loading = false,
                setlist = setlist,
                band = active.find { it.id == setlist?.bandId },
                event = event,
                rows = rowsOf(resolved),
                duration = calculateDuration(resolved, setlist?.pauseSeconds ?: 0),
            )
        }

    val state: StateFlow<SetlistDetailUiState> =
        combine(content, totalMode, reorderMode, deleted) { content, mode, reorder, deleted ->
            content.copy(totalMode = mode, reorderMode = reorder, deleted = deleted)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SetlistDetailUiState())

    fun setTotalMode(mode: TotalMode) {
        totalMode.value = mode
    }

    fun toggleReorderMode() = reorderMode.update { !it }

    fun increasePause() = updatePause(::increasePause)

    fun decreasePause() = updatePause(::decreasePause)

    private fun updatePause(change: (Int) -> Int) {
        val setlist = state.value.setlist ?: return
        val pause = change(setlist.pauseSeconds)
        if (pause == setlist.pauseSeconds) return
        viewModelScope.launch { setlists.save(setlist.copy(pauseSeconds = pause)) }
    }

    fun moveUp(id: SetlistItemId) = move(id, -1)

    fun moveDown(id: SetlistItemId) = move(id, 1)

    private fun move(
        id: SetlistItemId,
        delta: Int,
    ) {
        val order = state.value.rows.map { it.id }
        val from = order.indexOf(id)
        val newOrder = order.moved(from, from + delta)
        if (newOrder == order) return
        viewModelScope.launch { setlists.reorder(setlistId, newOrder) }
    }

    /** While dragging: [fromId] takes the place of [toId]. Saved by [onDragEnd]. */
    fun onDragMove(
        fromId: SetlistItemId,
        toId: SetlistItemId,
    ) {
        val order = pendingOrder.value ?: state.value.rows.map { it.id }
        pendingOrder.value = order.moved(order.indexOf(fromId), order.indexOf(toId))
    }

    fun onDragEnd() {
        val order = pendingOrder.value ?: return
        viewModelScope.launch {
            setlists.reorder(setlistId, order)
            // Keep showing the dragged order until the saved one arrives, so the list does not jump back.
            withTimeoutOrNull(SAVED_ORDER_TIMEOUT_MILLIS) {
                setlists.observeItems(setlistId).first { items -> items.map { it.id } == order }
            }
            pendingOrder.value = null
        }
    }

    /** Removes the item; a song stays in the catalogue. */
    fun removeItem(id: SetlistItemId) {
        viewModelScope.launch { setlists.removeItem(id) }
    }

    fun saveSongItem(draft: SongItemDraft) {
        viewModelScope.launch { setlists.updateItem(draft.applyTo()) }
    }

    /** Appended as the last item. */
    fun addStop(draft: StopDraft) {
        viewModelScope.launch { setlists.addItem(setlistId, draft.toItem(SetlistItemId(ids.newId()))) }
    }

    fun saveStop(draft: StopDraft) {
        val id = draft.itemId ?: return
        viewModelScope.launch { setlists.updateItem(draft.toItem(id)) }
    }

    /** Called after the user confirms. Songs stay; a linked event stays with no setlist. */
    fun delete() {
        viewModelScope.launch {
            setlists.delete(setlistId)
            deleted.value = true
        }
    }
}

/** Items in [order] when one is pending; items missing from it keep their place at the end. */
private fun inOrder(
    items: List<SetlistItem>,
    order: List<SetlistItemId>?,
): List<SetlistItem> {
    if (order == null) return items
    val rank = order.withIndex().associate { (index, id) -> id to index }
    return items.sortedBy { rank[it.id] ?: Int.MAX_VALUE }
}

private fun rowsOf(items: List<ResolvedItem>): List<ItemRow> {
    val markers = computeMarkers(items)
    var songNumber = 0
    return items.mapIndexed { index, entry ->
        ItemRow(
            entry = entry,
            songNumber = if (entry is ResolvedItem.SongEntry) ++songNumber else null,
            markers = markers[index],
            canMoveUp = index > 0,
            canMoveDown = index < items.lastIndex,
            nextIsSong = items.getOrNull(index + 1) is ResolvedItem.SongEntry,
        )
    }
}

private const val SAVED_ORDER_TIMEOUT_MILLIS = 2_000L
