package com.tochlabs.setlist.feature.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.setlist.ItemMarkers
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.domain.setlist.computeMarkers
import com.tochlabs.setlist.core.domain.setlist.effectiveBpm
import com.tochlabs.setlist.core.domain.setlist.effectiveKey
import com.tochlabs.setlist.core.domain.setlist.resolveItems
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One item as stage mode shows it, with the setlist's values (overrides applied). */
data class StageItem(
    val entry: ResolvedItem,
    /** Position among the songs; null for stops. */
    val songNumber: Int?,
    /** Null for stops. */
    val markers: ItemMarkers?,
) {
    val key: String? get() = (entry as? ResolvedItem.SongEntry)?.effectiveKey
    val bpm: Int? get() = (entry as? ResolvedItem.SongEntry)?.effectiveBpm

    /** Null when a song has no duration. */
    val durationSeconds: Int?
        get() =
            when (entry) {
                is ResolvedItem.SongEntry -> entry.song.durationSeconds
                is ResolvedItem.StopEntry -> entry.item.durationSeconds
            }

    val note: String?
        get() =
            when (entry) {
                is ResolvedItem.SongEntry -> entry.item.note
                is ResolvedItem.StopEntry -> entry.item.note
            }
}

data class StageUiState(
    val loading: Boolean = true,
    val setlistName: String = "",
    val items: List<StageItem> = emptyList(),
    /** Index of the current item. */
    val index: Int = 0,
) {
    /** Stage mode does not open on an empty setlist. */
    val empty: Boolean get() = !loading && items.isEmpty()
    val current: StageItem? get() = items.getOrNull(index)
    val nextItem: StageItem? get() = items.getOrNull(index + 1)

    /** 1-based, as shown in "1 / 9". */
    val position: Int get() = index + 1
    val total: Int get() = items.size
    val canGoBack: Boolean get() = index > 0
    val canGoForward: Boolean get() = index < items.lastIndex
}

/** One item at a time, with next and previous within the limits (specs/stage-mode). */
class StageViewModel(
    setlistId: SetlistId,
    setlists: SetlistRepository,
    songs: SongRepository,
) : ViewModel() {
    private val index = MutableStateFlow(0)

    val state: StateFlow<StageUiState> =
        combine(
            setlists.observe(setlistId),
            setlists.observeItems(setlistId),
            songs.observeSongs(),
            index,
        ) { setlist, items, catalogue, index ->
            val resolved = resolveItems(items, catalogue.associateBy { it.id })
            val markers = computeMarkers(resolved)
            var songNumber = 0
            val stageItems =
                resolved.mapIndexed { i, entry ->
                    StageItem(entry, if (entry is ResolvedItem.SongEntry) ++songNumber else null, markers[i])
                }
            StageUiState(
                loading = false,
                setlistName = setlist?.name.orEmpty(),
                items = stageItems,
                // Stays valid if items are removed while stage mode is open.
                index = index.coerceIn(0, (stageItems.size - 1).coerceAtLeast(0)),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), StageUiState())

    fun next() = goTo(state.value.index + 1)

    fun previous() = goTo(state.value.index - 1)

    /** Moves to [target] when it is an existing item; otherwise the current item stays. */
    fun goTo(target: Int) {
        if (target in state.value.items.indices) index.value = target
    }
}

private const val STOP_TIMEOUT_MILLIS = 5_000L
