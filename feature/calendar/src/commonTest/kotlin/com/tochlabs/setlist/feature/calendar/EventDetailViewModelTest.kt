package com.tochlabs.setlist.feature.calendar

import com.tochlabs.setlist.core.domain.validation.ValidationError
import com.tochlabs.setlist.core.model.MilestoneKind
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/event-calendar: "Roadmap of milestones", "Logistics", "Linked setlist".
class EventDetailViewModelTest {
    private val date = LocalDate(2026, 11, 20)

    @Test
    fun showLogistics_parkingAndContactAreInTheSheet() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date, parking = "Loading bay", contact = "Ana")
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            assertEquals(
                "Loading bay",
                vm.state.value.event
                    ?.parking,
            )
            assertEquals(
                "Ana",
                vm.state.value.event
                    ?.contact,
            )
            assertEquals(mareas, vm.state.value.band)
        }

    @Test
    fun rangeMilestone_soundcheckFrom20To21() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            val draft = MilestoneDraft().withKind(MilestoneKind.SOUNDCHECK)
            assertTrue(draft.hasEnd, "soundcheck is a range by default")
            vm.saveMilestone(draft.withStart(LocalTime(20, 0)).withEnd(LocalTime(21, 0)))

            val milestone =
                vm.state.value.milestones
                    .single()
            assertEquals(MilestoneKind.SOUNDCHECK, milestone.kind)
            assertEquals(LocalTime(20, 0), milestone.startTime)
            assertEquals(LocalTime(21, 0), milestone.endTime)
        }

    @Test
    fun milestonesAddedInAnyOrder_areListedByStartTime() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            vm.saveMilestone(MilestoneDraft().withKind(MilestoneKind.GIG).withStart(LocalTime(22, 0)).withoutEnd())
            vm.saveMilestone(MilestoneDraft().withKind(MilestoneKind.ARRIVAL).withStart(LocalTime(18, 30)))
            vm.saveMilestone(MilestoneDraft().withKind(MilestoneKind.ON_STAGE).withStart(LocalTime(21, 45)))

            assertEquals(
                listOf(MilestoneKind.ARRIVAL, MilestoneKind.ON_STAGE, MilestoneKind.GIG),
                vm.state.value.milestones
                    .map { it.kind },
            )
        }

    @Test
    fun customMilestone_dinnerIsListedWithTheOthers() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            val unnamed = MilestoneDraft().withKind(MilestoneKind.CUSTOM).withStart(LocalTime(19, 0))
            assertEquals(ValidationError.LABEL_REQUIRED, unnamed.error)
            vm.saveMilestone(unnamed)
            assertTrue(
                vm.state.value.milestones
                    .isEmpty(),
            )

            vm.saveMilestone(unnamed.withLabel("Dinner"))

            assertEquals(
                "Dinner",
                vm.state.value.milestones
                    .single()
                    .customLabel,
            )
        }

    @Test
    fun editRenameAndRemoveAMilestone() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)
            vm.saveMilestone(MilestoneDraft().withKind(MilestoneKind.ARRIVAL).withStart(LocalTime(18, 0)))

            val saved =
                vm.state.value.milestones
                    .single()
            vm.saveMilestone(MilestoneDraft.of(saved).withLabel("Meet at the van").withStart(LocalTime(17, 30)))

            val renamed =
                vm.state.value.milestones
                    .single()
            assertEquals(saved.id, renamed.id)
            assertEquals(MilestoneKind.ARRIVAL, renamed.kind)
            assertEquals("Meet at the van", renamed.customLabel)
            assertEquals(LocalTime(17, 30), renamed.startTime)

            vm.deleteMilestone(renamed.id)
            assertTrue(
                vm.state.value.milestones
                    .isEmpty(),
            )
        }

    @Test
    fun endBeforeStart_isNotSaved() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            val draft =
                MilestoneDraft()
                    .withKind(
                        MilestoneKind.GIG,
                    ).withStart(LocalTime(22, 0))
                    .withEnd(LocalTime(21, 0))
            assertEquals(ValidationError.END_BEFORE_START, draft.error)
            vm.saveMilestone(draft)

            assertTrue(
                vm.state.value.milestones
                    .isEmpty(),
            )
        }

    @Test
    fun linkASetlist_theSheetShowsItWithItsTotalWithStops() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val setlist = env.setlist("Concierto Sala Norte")
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            vm.linkSetlist(setlist.id)

            val linked = vm.state.value.linkedSetlist!!
            assertEquals(setlist, linked.setlist)
            assertEquals(8L * 60 + 40, linked.duration.totalWithStopsSeconds)
            assertEquals(
                setlist.id,
                env.store.events.value
                    .single()
                    .setlistId,
            )

            vm.linkSetlist(null)
            assertNull(vm.state.value.linkedSetlist)
        }

    @Test
    fun setlistsOfAnotherBandOrLinkedToAnotherEvent_areNotOffered() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val free = env.setlist("Libre")
            val taken = env.setlist("Ocupado")
            env.setlist("De los Sábados", band = sabados)
            env.event("Otro concierto", LocalDate(2026, 12, 1), setlistId = taken.id)
            val event = env.event("Concierto", date)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)

            assertEquals(listOf(free), vm.state.value.linkableSetlists)
        }

    @Test
    fun deleteTheEvent_keepsTheSetlist() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala")
            val event = env.event("Concierto", date, setlistId = setlist.id)
            val vm = env.detailViewModel(event.id)
            keepActive(vm.state)
            vm.saveMilestone(MilestoneDraft().withKind(MilestoneKind.ARRIVAL).withStart(LocalTime(18, 0)))

            vm.delete()

            assertTrue(vm.state.value.deleted)
            assertTrue(
                env.store.events.value
                    .isEmpty(),
            )
            assertTrue(
                env.store.milestones.value
                    .isEmpty(),
            )
            assertEquals(listOf(setlist), env.store.setlists.value)
        }
}
