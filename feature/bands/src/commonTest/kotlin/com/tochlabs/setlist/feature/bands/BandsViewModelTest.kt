package com.tochlabs.setlist.feature.bands

import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/bands: active list, "Archive a band".
class BandsViewModelTest {
    @Test
    fun theListShowsActiveBandsWithTheirSummary() =
        runViewModelTest {
            val env = BandsTestEnv()
            env.mareasWithData()
            val vm = env.listViewModel()
            keepActive(vm.state)

            val row =
                vm.state.value.bands
                    .single()
            assertEquals("Mareas", row.band.name)
            assertEquals(1, row.songCount)
            assertEquals(1, row.setlistCount)
            assertFalse(vm.state.value.loading)
        }

    @Test
    fun archiveABandWithSetlists_disappearsFromActiveScreensAndGoesToHistory() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.mareasWithData()
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.archive(mareas.id)

            assertTrue(
                vm.state.value.bands
                    .isEmpty(),
            )
            assertTrue(vm.state.value.hasHistory)
            assertEquals(BandsMessage.Archived("Mareas"), vm.state.value.message)
            assertTrue(
                env.setlists
                    .observeSetlists()
                    .first()
                    .isEmpty(),
            )
            assertEquals(
                listOf("Mareas"),
                env.bands
                    .observeArchived()
                    .first()
                    .map { it.name },
            )
        }

    @Test
    fun songsAreNotArchived() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.mareasWithData()
            val vm = env.listViewModel()

            vm.archive(mareas.id)

            assertEquals(
                listOf("Faro"),
                env.songs
                    .observeSongs()
                    .first()
                    .map { it.title },
            )
        }

    @Test
    fun theMessageIsClearedOnceShown() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.mareasWithData()
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.archive(mareas.id)
            vm.messageShown()

            assertNull(vm.state.value.message)
        }
}
