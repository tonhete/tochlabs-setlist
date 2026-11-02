package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

// specs/setlists: "Upcoming and past setlists".
class SetlistGroupsTest {
    private val today = LocalDate(2026, 11, 14)

    private fun setlist(
        name: String,
        date: LocalDate?,
    ) = Setlist(SetlistId(name), BandId("b"), name, date, pauseSeconds = 20)

    @Test
    fun upcomingAreOrderedByDateWithUndatedLast() {
        val groups =
            groupByDate(
                listOf(
                    setlist("undated", null),
                    setlist("later", LocalDate(2026, 12, 1)),
                    setlist("today", today),
                ),
                today,
            )
        assertEquals(listOf("today", "later", "undated"), groups.upcoming.map { it.name })
        assertEquals(emptyList(), groups.past)
    }

    @Test
    fun setlistsDatedBeforeTodayArePastNewestFirst() {
        val groups =
            groupByDate(
                listOf(
                    setlist("old", LocalDate(2026, 1, 10)),
                    setlist("yesterday", LocalDate(2026, 11, 13)),
                    setlist("next", LocalDate(2026, 11, 20)),
                ),
                today,
            )
        assertEquals(listOf("next"), groups.upcoming.map { it.name })
        assertEquals(listOf("yesterday", "old"), groups.past.map { it.name })
    }

    @Test
    fun sameDateKeepsNameOrder() {
        val groups = groupByDate(listOf(setlist("B", today), setlist("a", today)), today)
        assertEquals(listOf("a", "B"), groups.upcoming.map { it.name })
    }
}
