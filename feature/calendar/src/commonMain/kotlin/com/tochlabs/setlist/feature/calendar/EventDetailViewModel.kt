package com.tochlabs.setlist.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.setlist.DurationSummary
import com.tochlabs.setlist.core.domain.setlist.calculateDuration
import com.tochlabs.setlist.core.domain.setlist.resolveItems
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The setlist linked to the event, with its totals. */
data class LinkedSetlist(
    val setlist: Setlist,
    val duration: DurationSummary,
)

data class EventDetailUiState(
    val loading: Boolean = true,
    val event: Event? = null,
    val band: Band? = null,
    /** By start time. */
    val milestones: List<EventMilestone> = emptyList(),
    val linkedSetlist: LinkedSetlist? = null,
    /** Setlists of the event's band not linked to another event. */
    val linkableSetlists: List<Setlist> = emptyList(),
    val deleted: Boolean = false,
)

/** Event sheet: logistics, roadmap of milestones and linked setlist (specs/event-calendar). */
@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModel(
    private val eventId: EventId,
    private val events: EventRepository,
    bands: BandRepository,
    private val setlists: SetlistRepository,
    private val songs: SongRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val deleted = MutableStateFlow(false)
    private val event = events.observe(eventId)

    /** The linked setlist with its total; null when there is none. */
    private val linked: Flow<LinkedSetlist?> =
        event.map { it?.setlistId }.flatMapLatest { id ->
            if (id == null) flowOf(null) else linkedSetlist(id)
        }

    private fun linkedSetlist(id: SetlistId): Flow<LinkedSetlist?> =
        combine(setlists.observe(id), setlists.observeItems(id), songs.observeSongs()) { setlist, items, catalogue ->
            setlist?.let {
                val resolved = resolveItems(items, catalogue.associateBy { song -> song.id })
                LinkedSetlist(it, calculateDuration(resolved, it.pauseSeconds))
            }
        }

    /** Setlists of the band, without those linked to another event (one event per setlist). */
    private val linkable: Flow<List<Setlist>> =
        event.map { it?.bandId }.flatMapLatest { bandId ->
            if (bandId == null) {
                flowOf(emptyList())
            } else {
                combine(setlists.observeSetlists(bandId), events.observeEvents(bandId)) { candidates, bandEvents ->
                    val taken = bandEvents.filter { it.id != eventId }.mapNotNull { it.setlistId }.toSet()
                    candidates.filterNot { it.id in taken }
                }
            }
        }

    private val content =
        combine(event, bands.observeActive(), events.observeMilestones(eventId), linked, linkable) {
            event,
            active,
            milestones,
            linked,
            linkable,
            ->
            EventDetailUiState(
                loading = false,
                event = event,
                band = active.find { it.id == event?.bandId },
                milestones = milestones,
                linkedSetlist = linked,
                linkableSetlists = linkable,
            )
        }

    val state: StateFlow<EventDetailUiState> =
        combine(content, deleted) { content, deleted -> content.copy(deleted = deleted) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EventDetailUiState())

    /** Saves a valid draft; an invalid one is ignored (the sheet shows its error). */
    fun saveMilestone(draft: MilestoneDraft) {
        val input = (draft.validation as? Validated.Valid)?.value ?: return
        val milestone =
            EventMilestone(
                id = draft.id ?: EventMilestoneId(ids.newId()),
                eventId = eventId,
                kind = input.kind,
                customLabel = input.label,
                startTime = input.start,
                endTime = input.end,
            )
        viewModelScope.launch { events.saveMilestone(milestone) }
    }

    fun deleteMilestone(id: EventMilestoneId) {
        viewModelScope.launch { events.deleteMilestone(id) }
    }

    /** Links [setlistId], or unlinks when null. */
    fun linkSetlist(setlistId: SetlistId?) {
        val current = state.value.event ?: return
        viewModelScope.launch { events.save(current.copy(setlistId = setlistId)) }
    }

    /** Called after the user confirms. Milestones go with the event; a linked setlist stays. */
    fun delete() {
        viewModelScope.launch {
            events.delete(eventId)
            deleted.value = true
        }
    }
}
