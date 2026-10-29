package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.MilestoneKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class EventRepositoryTest : RepositoryTestBase() {
    private val mareas = band("Mareas")

    private fun milestone(
        id: String,
        event: String,
        kind: MilestoneKind,
        start: LocalTime,
        end: LocalTime? = null,
    ) = EventMilestone(
        EventMilestoneId(id),
        EventId(event),
        kind,
        null,
        start,
        end,
    )

    @Test
    fun eventIsStoredWithItsDetails() =
        runTest {
            bands.save(mareas)
            val gig = event("Concierto", mareas).copy(venue = "Sala Norte", parking = "Calle B")
            events.save(gig)
            assertEquals(listOf(gig), events.observeEvents(mareas.id).first())
        }

    @Test
    fun milestonesAreOrderedByStartTime() =
        runTest {
            bands.save(mareas)
            events.save(event("Concierto", mareas))
            events.saveMilestone(milestone("m2", "Concierto", MilestoneKind.GIG, LocalTime(21, 30), LocalTime(23, 0)))
            events.saveMilestone(milestone("m1", "Concierto", MilestoneKind.SOUNDCHECK, LocalTime(18, 0)))

            val kinds = events.observeMilestones(event("Concierto", mareas).id).first().map { it.kind }
            assertEquals(listOf(MilestoneKind.SOUNDCHECK, MilestoneKind.GIG), kinds)
        }

    @Test
    fun deletingAnEventDeletesItsMilestones() =
        runTest {
            bands.save(mareas)
            events.save(event("Concierto", mareas))
            events.saveMilestone(milestone("m1", "Concierto", MilestoneKind.ARRIVAL, LocalTime(17, 0)))

            events.delete(event("Concierto", mareas).id)

            assertNull(events.observe(event("Concierto", mareas).id).first())
            assertTrue(events.observeMilestones(event("Concierto", mareas).id).first().isEmpty())
        }

    @Test
    fun eventLinkedToASetlistIsFoundFromTheSetlist() =
        runTest {
            bands.save(mareas)
            val salaNorte = setlist("Sala Norte", mareas)
            setlists.save(salaNorte)
            events.save(event("Concierto", mareas, salaNorte))

            assertEquals("Concierto", events.observeBySetlist(salaNorte.id).first()?.title)
        }
}
