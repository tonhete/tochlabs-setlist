package com.tochlabs.setlist.feature.bands

import com.tochlabs.setlist.core.domain.repository.BandDeletionSummary
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/bands: "Restore a band", "Permanent deletion only from the history".
class BandHistoryViewModelTest {
    private suspend fun BandsTestEnv.archivedMareas() = mareasWithData().also { bands.archive(it.id) }

    @Test
    fun restoreFromHistory_bringsBackTheBandItsSetlistsAndEvents() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.archivedMareas()
            val vm = env.historyViewModel()
            keepActive(vm.state)

            vm.restore(mareas.id)

            assertTrue(
                vm.state.value.bands
                    .isEmpty(),
            )
            assertEquals(HistoryMessage.Restored("Mareas"), vm.state.value.message)
            assertEquals(
                listOf("Mareas"),
                env.bands
                    .observeActive()
                    .first()
                    .map { it.name },
            )
            assertEquals(
                listOf("Sala Norte"),
                env.setlists
                    .observeSetlists()
                    .first()
                    .map { it.name },
            )
            assertEquals(1, env.store.events.value.size)
        }

    @Test
    fun confirmBeforeDeleting_showsWhatWillBeRemoved() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.archivedMareas()
            val vm = env.historyViewModel()
            keepActive(vm.state)

            vm.requestDelete(mareas.id)

            val pending = vm.state.value.pendingDeletion
            assertEquals(mareas.id, pending?.band?.id)
            assertEquals(BandDeletionSummary(setlists = 1, events = 1, repertoireEntries = 1), pending?.summary)
            assertEquals(
                listOf("Mareas"),
                env.bands
                    .observeArchived()
                    .first()
                    .map { it.name },
            )
        }

    @Test
    fun cancelDeletion_deletesNothing() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.archivedMareas()
            val vm = env.historyViewModel()
            keepActive(vm.state)

            vm.requestDelete(mareas.id)
            vm.cancelDelete()

            assertNull(vm.state.value.pendingDeletion)
            assertEquals(
                listOf("Mareas"),
                vm.state.value.bands
                    .map { it.name },
            )
            assertEquals(1, env.store.setlists.value.size)
        }

    @Test
    fun confirmedDeletion_removesBandSetlistsEventsAndRepertoireButKeepsSongs() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.archivedMareas()
            val vm = env.historyViewModel()
            keepActive(vm.state)

            vm.requestDelete(mareas.id)
            vm.confirmDelete()

            assertNull(vm.state.value.pendingDeletion)
            assertTrue(
                vm.state.value.bands
                    .isEmpty(),
            )
            assertEquals(HistoryMessage.Deleted("Mareas"), vm.state.value.message)
            assertTrue(
                env.store.bands.value
                    .isEmpty(),
            )
            assertTrue(
                env.store.setlists.value
                    .isEmpty(),
            )
            assertTrue(
                env.store.events.value
                    .isEmpty(),
            )
            assertTrue(
                env.store.repertoire.value
                    .isEmpty(),
            )
            assertEquals(
                listOf("Faro"),
                env.songs
                    .observeSongs()
                    .first()
                    .map { it.title },
            )
        }
}
