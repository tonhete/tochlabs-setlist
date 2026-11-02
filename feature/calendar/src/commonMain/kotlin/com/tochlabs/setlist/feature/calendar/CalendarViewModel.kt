package com.tochlabs.setlist.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.calendar.firstDayOfMonth
import com.tochlabs.setlist.core.domain.calendar.isInMonthOf
import com.tochlabs.setlist.core.domain.calendar.monthGrid
import com.tochlabs.setlist.core.domain.calendar.plusMonths
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** One cell of the monthly grid. */
data class CalendarDay(
    val date: LocalDate,
    /** False for the days of the neighbouring months that complete the first and last week. */
    val inMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    /** One colour per event of the day, in agenda order; empty when there are none. */
    val bandColors: List<BandColor>,
)

data class AgendaEntry(
    val event: Event,
    val band: Band,
)

data class CalendarUiState(
    val loading: Boolean = true,
    /** Active bands, offered as filter chips. */
    val bands: List<Band> = emptyList(),
    val selectedBand: BandId? = null,
    /** First day of the month shown. */
    val month: LocalDate = LocalDate.fromEpochDays(0),
    val weeks: List<List<CalendarDay>> = emptyList(),
    /** Null: the agenda lists the whole month. */
    val selectedDay: LocalDate? = null,
    val agenda: List<AgendaEntry> = emptyList(),
) {
    val noBands: Boolean get() = !loading && bands.isEmpty()
}

/** Calendar tab root: monthly grid and agenda with a band filter (specs/event-calendar). */
class CalendarViewModel(
    events: EventRepository,
    bands: BandRepository,
    private val clock: Clock,
) : ViewModel() {
    private val view = MutableStateFlow(View(month = firstDayOfMonth(clock.today()), day = null))
    private val selectedBand = MutableStateFlow<BandId?>(null)
    private val activeBands = bands.observeActive()

    /** Month shown and selected day, changed together so they never disagree. */
    private data class View(
        val month: LocalDate,
        val day: LocalDate?,
    )

    val state: StateFlow<CalendarUiState> =
        combine(activeBands, events.observeEvents(), view, selectedBand) { active, all, view, selected ->
            // Events of archived bands are not returned by the repository; a band archived while
            // selected is no longer a valid filter.
            val band = selected?.takeIf { id -> active.any { it.id == id } }
            val bandsById = active.associateBy { it.id }
            val visible =
                all.mapNotNull { event ->
                    bandsById[event.bandId]
                        ?.takeIf { band == null || it.id == band }
                        ?.let { AgendaEntry(event, it) }
                }
            val byDate = visible.groupBy { it.event.date }
            val today = clock.today()
            CalendarUiState(
                loading = false,
                bands = active,
                selectedBand = band,
                month = view.month,
                weeks =
                    monthGrid(view.month).map { week ->
                        week.map { date ->
                            CalendarDay(
                                date = date,
                                inMonth = date.isInMonthOf(view.month),
                                isToday = date == today,
                                isSelected = date == view.day,
                                bandColors = byDate[date].orEmpty().map { it.band.color },
                            )
                        }
                    },
                selectedDay = view.day,
                agenda =
                    if (view.day != null) {
                        byDate[view.day].orEmpty()
                    } else {
                        visible.filter { it.event.date.isInMonthOf(view.month) }
                    },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CalendarUiState())

    fun previousMonth() = view.update { View(it.month.plusMonths(-1), day = null) }

    fun nextMonth() = view.update { View(it.month.plusMonths(1), day = null) }

    /** Selects [date], moving to its month; selecting the selected day again shows the whole month. */
    fun selectDay(date: LocalDate) =
        view.update { current ->
            if (current.day == date) current.copy(day = null) else View(firstDayOfMonth(date), date)
        }

    fun showToday() {
        val today = clock.today()
        view.value = View(firstDayOfMonth(today), today)
    }

    fun selectBand(id: BandId?) {
        selectedBand.value = id
    }
}

internal const val STOP_TIMEOUT_MILLIS = 5_000L
