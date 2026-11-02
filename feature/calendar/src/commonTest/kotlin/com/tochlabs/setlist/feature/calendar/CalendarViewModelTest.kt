package com.tochlabs.setlist.feature.calendar

import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/event-calendar: "Calendar and agenda", "Archived bands"; "Create a gig" (appears on its date).
class CalendarViewModelTest {
    private fun CalendarUiState.day(date: LocalDate) = weeks.flatten().single { it.date == date }

    @Test
    fun opensOnTheCurrentMonthStartingOnMonday() =
        runViewModelTest {
            val vm = CalendarTestEnv().calendarViewModel()
            keepActive(vm.state)

            val state = vm.state.value
            assertEquals(LocalDate(2026, 11, 1), state.month)
            assertEquals(
                LocalDate(2026, 10, 26),
                state.weeks
                    .first()
                    .first()
                    .date,
            )
            assertTrue(state.day(TODAY).isToday)
            assertFalse(state.day(LocalDate(2026, 10, 26)).inMonth)
            assertNull(state.selectedDay)
        }

    @Test
    fun createAGig_appearsInTheCalendarOnThatDate() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Concierto Sala Norte", LocalDate(2026, 11, 20))
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            assertEquals(
                listOf(mareas.color),
                vm.state.value
                    .day(LocalDate(2026, 11, 20))
                    .bandColors,
            )
            assertTrue(
                vm.state.value
                    .day(LocalDate(2026, 11, 21))
                    .bandColors
                    .isEmpty(),
            )
        }

    @Test
    fun noDaySelected_theAgendaListsTheWholeMonth() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Ensayo", LocalDate(2026, 11, 3), band = sabados)
            env.event("Concierto", LocalDate(2026, 11, 20))
            env.event("Diciembre", LocalDate(2026, 12, 2))
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            val agenda = vm.state.value.agenda
            assertEquals(listOf("Ensayo", "Concierto"), agenda.map { it.event.title })
            assertEquals(sabados, agenda.first().band)
        }

    @Test
    fun selectADayWithTwoEvents_theAgendaListsThem() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Prueba", LocalDate(2026, 11, 20), band = sabados)
            env.event("Concierto", LocalDate(2026, 11, 20))
            env.event("Otro día", LocalDate(2026, 11, 21))
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            vm.selectDay(LocalDate(2026, 11, 20))

            assertEquals(LocalDate(2026, 11, 20), vm.state.value.selectedDay)
            assertTrue(
                vm.state.value
                    .day(LocalDate(2026, 11, 20))
                    .isSelected,
            )
            assertEquals(
                listOf("Concierto", "Prueba"),
                vm.state.value.agenda
                    .map { it.event.title },
            )
        }

    @Test
    fun dayWithoutEvents_theAgendaIsEmpty() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Concierto", LocalDate(2026, 11, 20))
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            vm.selectDay(LocalDate(2026, 11, 18))

            assertTrue(
                vm.state.value.agenda
                    .isEmpty(),
            )
        }

    @Test
    fun selectingTheSelectedDayAgain_showsTheWholeMonth() =
        runViewModelTest {
            val env = CalendarTestEnv()
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            vm.selectDay(LocalDate(2026, 11, 18))
            vm.selectDay(LocalDate(2026, 11, 18))

            assertNull(vm.state.value.selectedDay)
        }

    @Test
    fun selectingADayOfTheNextMonth_movesToThatMonth() =
        runViewModelTest {
            val vm = CalendarTestEnv().calendarViewModel()
            keepActive(vm.state)

            vm.selectDay(LocalDate(2026, 12, 3))

            assertEquals(LocalDate(2026, 12, 1), vm.state.value.month)
            assertEquals(LocalDate(2026, 12, 3), vm.state.value.selectedDay)
        }

    @Test
    fun monthNavigation_clearsTheSelectedDay() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Diciembre", LocalDate(2026, 12, 2))
            val vm = env.calendarViewModel()
            keepActive(vm.state)
            vm.selectDay(LocalDate(2026, 11, 18))

            vm.nextMonth()

            assertEquals(LocalDate(2026, 12, 1), vm.state.value.month)
            assertNull(vm.state.value.selectedDay)
            assertEquals(
                listOf("Diciembre"),
                vm.state.value.agenda
                    .map { it.event.title },
            )

            vm.previousMonth()
            vm.previousMonth()
            assertEquals(LocalDate(2026, 10, 1), vm.state.value.month)

            vm.showToday()
            assertEquals(LocalDate(2026, 11, 1), vm.state.value.month)
            assertEquals(TODAY, vm.state.value.selectedDay)
        }

    @Test
    fun theCalendarRespectsTheBandFilter() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Prueba", LocalDate(2026, 11, 20), band = sabados)
            env.event("Concierto", LocalDate(2026, 11, 20))
            val vm = env.calendarViewModel()
            keepActive(vm.state)

            vm.selectBand(sabados.id)

            assertEquals(
                listOf("Prueba"),
                vm.state.value.agenda
                    .map { it.event.title },
            )
            assertEquals(
                listOf(sabados.color),
                vm.state.value
                    .day(LocalDate(2026, 11, 20))
                    .bandColors,
            )
        }

    @Test
    fun archiveHidesEvents_andRestoreShowsThemAgain() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            env.event("Concierto", LocalDate(2026, 11, 20))
            val vm = env.calendarViewModel()
            keepActive(vm.state)
            vm.selectBand(mareas.id)

            env.bands.archive(mareas.id)

            assertTrue(
                vm.state.value.agenda
                    .isEmpty(),
            )
            assertTrue(
                vm.state.value
                    .day(LocalDate(2026, 11, 20))
                    .bandColors
                    .isEmpty(),
            )
            assertNull(vm.state.value.selectedBand)

            env.bands.restore(mareas.id)

            assertEquals(
                listOf("Concierto"),
                vm.state.value.agenda
                    .map { it.event.title },
            )
        }
}
