package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlists: "Create and edit setlists" (listing), "Upcoming and past setlists"; specs/bands: band filter.
class SetlistsViewModelTest {
    @Test
    fun createASetlist_isListedWithItsDateAndBand() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Concierto Sala Norte", LocalDate(2026, 11, 20))
            val vm = env.listViewModel()
            keepActive(vm.state)

            val card =
                vm.state.value.upcoming
                    .single()
            assertEquals("Concierto Sala Norte", card.setlist.name)
            assertEquals(LocalDate(2026, 11, 20), card.setlist.date)
            assertEquals(mareas, card.band)
            assertFalse(vm.state.value.loading)
        }

    @Test
    fun setlistWithoutDate_isUpcomingAndListedLast() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Sin fecha")
            env.setlist("Fiestas", LocalDate(2026, 12, 5))
            env.setlist("Hoy", TODAY)
            val vm = env.listViewModel()
            keepActive(vm.state)

            val upcoming = vm.state.value.upcoming
            assertEquals(listOf("Hoy", "Fiestas", "Sin fecha"), upcoming.map { it.setlist.name })
            assertNull(upcoming.last().setlist.date)
        }

    @Test
    fun pastSetlistsCollapsed_onlyUpcomingAreShownWithThePastCount() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Próximo", LocalDate(2026, 11, 20))
            env.setlist("Verano", LocalDate(2026, 7, 1))
            env.setlist("Ayer", LocalDate(2026, 11, 13))
            val vm = env.listViewModel()
            keepActive(vm.state)

            val state = vm.state.value
            assertEquals(listOf("Próximo"), state.upcoming.map { it.setlist.name })
            assertEquals(2, state.pastCount)
            assertFalse(state.showPast)
            assertTrue(state.past.isEmpty())
        }

    @Test
    fun showPastSetlists_listsThemNewestFirst() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Verano", LocalDate(2026, 7, 1))
            env.setlist("Ayer", LocalDate(2026, 11, 13))
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.togglePast()

            assertTrue(vm.state.value.showPast)
            assertEquals(
                listOf("Ayer", "Verano"),
                vm.state.value.past
                    .map { it.setlist.name },
            )
        }

    @Test
    fun filterByBand_listsOnlyThatBandsSetlists() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Mareas en sala", band = mareas)
            env.setlist("Sábado en fiestas", band = sabados)
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.selectBand(sabados.id)

            assertEquals(sabados.id, vm.state.value.selectedBand)
            assertEquals(
                listOf("Sábado en fiestas"),
                vm.state.value.upcoming
                    .map { it.setlist.name },
            )
        }

    @Test
    fun setlistsOfArchivedBandsAreNotListed() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlist("Mareas en sala", band = mareas)
            env.setlist("Sábado en fiestas", band = sabados)
            val vm = env.listViewModel()
            keepActive(vm.state)
            vm.selectBand(mareas.id)

            env.bands.archive(mareas.id)

            assertNull(vm.state.value.selectedBand)
            assertEquals(
                listOf("Sábado en fiestas"),
                vm.state.value.upcoming
                    .map { it.setlist.name },
            )
        }

    @Test
    fun cardShowsSongCountTotalWithStopsAndRoadmapBadge() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte", pauseSeconds = 20)
            env.addSong(setlist, env.song("Uno", minutes(3)))
            env.addStop(setlist, StopType.SPEECH, minutes(1))
            env.addSong(setlist, env.song("Dos", minutes(4)))
            env.linkEvent(setlist)
            env.setlist("Vacío")
            val vm = env.listViewModel()
            keepActive(vm.state)

            val cards =
                vm.state.value.upcoming
                    .associateBy { it.setlist.name }
            val card = cards.getValue("Sala Norte")
            assertEquals(2, card.songCount)
            assertEquals(minutes(8, 40).toLong(), card.duration.totalWithStopsSeconds)
            assertTrue(card.hasRoadmap)
            assertFalse(cards.getValue("Vacío").hasRoadmap)
            assertEquals(0, cards.getValue("Vacío").songCount)
        }

    @Test
    fun noActiveBands_theListAsksToCreateOneFirst() =
        runViewModelTest {
            val vm = SetlistsTestEnv().listViewModel()
            keepActive(vm.state)
            assertTrue(vm.state.value.noBands)
        }
}
