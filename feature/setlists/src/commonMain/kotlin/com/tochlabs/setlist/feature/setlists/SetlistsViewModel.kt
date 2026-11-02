package com.tochlabs.setlist.feature.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.setlist.DurationSummary
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.domain.setlist.calculateDuration
import com.tochlabs.setlist.core.domain.setlist.groupByDate
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
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

/** One setlist in the list, with what its card shows. */
data class SetlistCard(
    val setlist: Setlist,
    val band: Band,
    val songCount: Int,
    val duration: DurationSummary,
    /** An event is linked to the setlist. */
    val hasRoadmap: Boolean,
)

data class SetlistsUiState(
    val loading: Boolean = true,
    /** Active bands, offered as filter chips. */
    val bands: List<Band> = emptyList(),
    val selectedBand: BandId? = null,
    val upcoming: List<SetlistCard> = emptyList(),
    /** Empty while the past group is collapsed; [pastCount] is always known. */
    val past: List<SetlistCard> = emptyList(),
    val pastCount: Int = 0,
    val showPast: Boolean = false,
) {
    /** A setlist needs an active band; without any the list asks to create one. */
    val noBands: Boolean get() = !loading && bands.isEmpty()
}

/** Setlists tab root: upcoming and past setlists with a band filter (specs/setlists). */
@OptIn(ExperimentalCoroutinesApi::class)
class SetlistsViewModel(
    private val setlists: SetlistRepository,
    bands: BandRepository,
    private val songs: SongRepository,
    events: EventRepository,
    private val clock: Clock,
) : ViewModel() {
    private val selectedBand = MutableStateFlow<BandId?>(null)
    private val showPast = MutableStateFlow(false)
    private val activeBands = bands.observeActive()

    /** Items of every listed setlist, by setlist. */
    private val itemsBySetlist: Flow<Map<SetlistId, List<SetlistItem>>> =
        setlists.observeSetlists().flatMapLatest { all ->
            if (all.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(all.map { setlist -> setlists.observeItems(setlist.id).map { setlist.id to it } }) {
                    it.toMap()
                }
            }
        }

    private val cards: Flow<List<SetlistCard>> =
        combine(
            setlists.observeSetlists(),
            activeBands,
            itemsBySetlist,
            songs.observeSongs(),
            events.observeEvents(),
        ) { all, active, items, catalogue, events ->
            val bandsById = active.associateBy { it.id }
            val songsById = catalogue.associateBy { it.id }
            val withEvent = events.mapNotNull { it.setlistId }.toSet()
            all.mapNotNull { setlist ->
                val band = bandsById[setlist.bandId] ?: return@mapNotNull null
                val resolved = resolveItems(items[setlist.id].orEmpty(), songsById)
                SetlistCard(
                    setlist = setlist,
                    band = band,
                    songCount = resolved.count { it is ResolvedItem.SongEntry },
                    duration = calculateDuration(resolved, setlist.pauseSeconds),
                    hasRoadmap = setlist.id in withEvent,
                )
            }
        }

    val state: StateFlow<SetlistsUiState> =
        combine(activeBands, cards, selectedBand, showPast) { active, cards, selected, showPast ->
            // A band archived while selected is no longer a valid filter.
            val band = selected?.takeIf { id -> active.any { it.id == id } }
            val visible = cards.filter { band == null || it.setlist.bandId == band }
            val byId = visible.associateBy { it.setlist.id }
            val groups = groupByDate(visible.map { it.setlist }, clock.today())
            SetlistsUiState(
                loading = false,
                bands = active,
                selectedBand = band,
                upcoming = groups.upcoming.map { byId.getValue(it.id) },
                past = if (showPast) groups.past.map { byId.getValue(it.id) } else emptyList(),
                pastCount = groups.past.size,
                showPast = showPast,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SetlistsUiState())

    fun selectBand(id: BandId?) {
        selectedBand.value = id
    }

    fun togglePast() {
        showPast.value = !showPast.value
    }
}

internal const val STOP_TIMEOUT_MILLIS = 5_000L
