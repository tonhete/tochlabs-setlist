package com.tochlabs.setlist.feature.share

import com.tochlabs.setlist.core.testing.FakeSetlistFileCodec
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlist-sharing: "Import a .setlist file", "Reject invalid files".
class ImportViewModelTest {
    /** Exports the sample setlist through the fake codec and leaves it in the inbox, as opening a file does. */
    private suspend fun ShareTestEnv.receiveSalaNorte() {
        val setlist = salaNorte()
        val pkg = transfer.exportPackage(setlist.id)!!
        inbox.offer(codec.encode(pkg))
    }

    @Test
    fun aValidFile_showsWhatItContains() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.receiveSalaNorte()
            val vm = env.importViewModel()
            keepActive(vm.state)

            val preview = vm.state.value.preview!!
            assertEquals("Concierto Sala Norte", preview.setlistName)
            assertEquals("Mareas", preview.bandName)
            assertEquals(2, preview.songCount)
            assertEquals(1, preview.stopCount)
            assertNull(vm.state.value.error)
            assertNull(env.inbox.take(), "the file was taken from the inbox")
        }

    @Test
    fun importIntoABand_createsANewSetlistForThatBand() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.receiveSalaNorte()
            val vm = env.importViewModel()
            keepActive(vm.state)

            vm.selectBand(sabados.id)
            vm.import()

            val id = assertNotNull(vm.state.value.importedId)
            val created =
                env.store.setlists.value
                    .single { it.id == id }
            assertEquals(sabados.id, created.bandId)
            assertEquals("Concierto Sala Norte", created.name)
            assertEquals(
                3,
                env.store.items.value
                    .getValue(id)
                    .size,
            )
            assertEquals(2, env.store.setlists.value.size, "the original setlist stays")
        }

    @Test
    fun aBandMustBeChosen() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.receiveSalaNorte()
            val vm = env.importViewModel()
            keepActive(vm.state)

            vm.import()

            assertTrue(vm.state.value.bandError)
            assertEquals(1, env.store.setlists.value.size)
        }

    @Test
    fun onlyActiveBandsAreOffered() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.receiveSalaNorte()
            env.bands.archive(sabados.id)
            val vm = env.importViewModel()
            keepActive(vm.state)

            assertEquals(listOf(mareas), vm.state.value.bands)
            assertEquals(mareas.id, vm.state.value.bandId, "the only active band is preselected")
        }

    @Test
    fun corruptFile_nothingIsImportedAndTheUserIsTold() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.salaNorte()
            env.inbox.offer(FakeSetlistFileCodec.UNREADABLE)
            val vm = env.importViewModel()
            keepActive(vm.state)

            assertEquals(ImportError.UNREADABLE, vm.state.value.error)
            vm.selectBand(mareas.id)
            vm.import()
            assertNull(vm.state.value.importedId)
            assertEquals(1, env.store.setlists.value.size)
        }

    @Test
    fun newerFormat_theUserIsToldToUpdateTheApp() =
        runViewModelTest {
            val env = ShareTestEnv()
            env.inbox.offer(FakeSetlistFileCodec.NEWER)
            val vm = env.importViewModel()
            keepActive(vm.state)

            assertEquals(ImportError.NEWER_VERSION, vm.state.value.error)
        }

    @Test
    fun noFileWaiting_isReportedAsUnreadable() =
        runViewModelTest {
            val vm = ShareTestEnv().importViewModel()
            keepActive(vm.state)
            assertEquals(ImportError.UNREADABLE, vm.state.value.error)
        }
}
