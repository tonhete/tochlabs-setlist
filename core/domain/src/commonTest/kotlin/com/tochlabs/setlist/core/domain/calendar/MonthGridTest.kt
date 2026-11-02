package com.tochlabs.setlist.core.domain.calendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// specs/event-calendar: "Calendar and agenda" (monthly grid starting on Monday, docs/product/screens-and-flows.md).
class MonthGridTest {
    @Test
    fun weeksStartOnMondayAndCoverTheWholeMonth() {
        // 1 Nov 2026 is a Sunday.
        val weeks = monthGrid(LocalDate(2026, 11, 14))

        assertEquals(LocalDate(2026, 10, 26), weeks.first().first())
        assertEquals(LocalDate(2026, 12, 6), weeks.last().last())
        assertEquals(6, weeks.size)
        assertTrue(weeks.all { it.size == 7 && it.first().dayOfWeek == DayOfWeek.MONDAY })
    }

    @Test
    fun aMonthStartingOnMondayHasNoDaysOfThePreviousMonth() {
        // 1 Jun 2026 is a Monday; 30 Jun is a Tuesday.
        val weeks = monthGrid(LocalDate(2026, 6, 1))

        assertEquals(LocalDate(2026, 6, 1), weeks.first().first())
        assertEquals(LocalDate(2026, 7, 5), weeks.last().last())
        assertEquals(5, weeks.size)
    }

    @Test
    fun monthNavigation() {
        assertEquals(LocalDate(2026, 12, 1), firstDayOfMonth(LocalDate(2026, 11, 30)).plusMonths(1))
        assertEquals(LocalDate(2027, 1, 1), LocalDate(2026, 12, 1).plusMonths(1))
        assertEquals(LocalDate(2026, 12, 1), LocalDate(2027, 1, 1).plusMonths(-1))
    }

    @Test
    fun sameMonth() {
        assertTrue(LocalDate(2026, 11, 1).isInMonthOf(LocalDate(2026, 11, 30)))
        assertTrue(!LocalDate(2026, 10, 31).isInMonthOf(LocalDate(2026, 11, 30)))
        assertTrue(!LocalDate(2025, 11, 1).isInMonthOf(LocalDate(2026, 11, 30)))
    }
}
