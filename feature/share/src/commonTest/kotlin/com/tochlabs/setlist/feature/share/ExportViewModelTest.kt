package com.tochlabs.setlist.feature.share

import com.tochlabs.setlist.core.domain.transfer.SetlistFileResult
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.testing.runViewModelTest
import com.tochlabs.setlist.feature.share.pdf.PdfLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlist-export ("Share and print") and specs/setlist-sharing ("Export a setlist file").
class ExportViewModelTest {
    private fun ExportViewModel.firstLine(env: ShareTestEnv) =
        (
            env.renderer.lastLayout!!
                .pages
                .first()
                .blocks
                .first()
                .lines
                .first() as PdfLine.Text
        ).text

    @Test
    fun theSummaryShowsSongsStopsAndBand() =
        runViewModelTest {
            val env = ShareTestEnv()
            val setlist = env.salaNorte()
            val vm = env.exportViewModel(setlist.id)

            val state = vm.state.value
            assertEquals("Concierto Sala Norte", state.setlistName)
            assertEquals("Mareas", state.bandName)
            assertEquals(2, state.songCount)
            assertEquals(1, state.stopCount)
            assertEquals(ExportMode.STAGE_PDF, state.mode)
        }

    @Test
    fun shareTheStagePdf_opensTheShareSheetWithThePdf() =
        runViewModelTest {
            val env = ShareTestEnv()
            val setlist = env.salaNorte()
            val vm = env.exportViewModel(setlist.id)

            vm.share()

            val ready = vm.state.value.ready!!
            assertEquals(ExportAction.SHARE, ready.action)
            assertEquals("application/pdf", ready.file.mimeType)
            assertEquals("Concierto Sala Norte 2026-11-20 - stage.pdf", ready.file.name)
            assertEquals("%PDF", ready.file.bytes.decodeToString())
            assertEquals("Concierto Sala Norte", vm.firstLine(env))
        }

    @Test
    fun printTheListPdf_opensThePrintDialogWithThatLayout() =
        runViewModelTest {
            val env = ShareTestEnv()
            val setlist = env.salaNorte()
            val vm = env.exportViewModel(setlist.id)

            vm.selectMode(ExportMode.LIST_PDF)
            vm.print()

            val ready = vm.state.value.ready!!
            assertEquals(ExportAction.PRINT, ready.action)
            assertEquals("Concierto Sala Norte 2026-11-20 - list.pdf", ready.file.name)
            assertTrue(
                env.renderer.lastLayout!!
                    .columnWeights
                    .isNotEmpty(),
                "the list layout has a table",
            )

            vm.consumed()
            assertNull(vm.state.value.ready)
        }

    @Test
    fun shareAsAFile_createsADescriptiveSetlistFileWithTheWholeSetlist() =
        runViewModelTest {
            val env = ShareTestEnv()
            val setlist = env.salaNorte()
            val vm = env.exportViewModel(setlist.id)

            vm.selectMode(ExportMode.BAND_FILE)
            assertFalse(vm.state.value.canPrint)
            vm.share()

            val file =
                vm.state.value.ready!!
                    .file
            assertEquals("Concierto Sala Norte 2026-11-20 - setlist.json", file.name)
            assertEquals("application/json", file.mimeType)
            val decoded = env.codec.decode(file.bytes.decodeToString()) as SetlistFileResult.Valid
            assertEquals(3, decoded.pkg.items.size)
            assertEquals("Mareas", decoded.pkg.bandName)
        }

    @Test
    fun theBandFileCannotBePrinted() =
        runViewModelTest {
            val env = ShareTestEnv()
            val setlist = env.salaNorte()
            val vm = env.exportViewModel(setlist.id)

            vm.selectMode(ExportMode.BAND_FILE)
            vm.print()

            assertNull(vm.state.value.ready)
        }

    @Test
    fun fileNamesDropCharactersFileSystemsReject() {
        assertEquals("AC-DC tribute- live", safeFileName("AC/DC tribute: live"))
        assertEquals("setlist", safeFileName("  ?*  "))
    }

    @Test
    fun aMissingSetlist_isReported() =
        runViewModelTest {
            val vm = ShareTestEnv().exportViewModel(SetlistId("gone"))
            assertTrue(vm.state.value.missing)
        }
}
