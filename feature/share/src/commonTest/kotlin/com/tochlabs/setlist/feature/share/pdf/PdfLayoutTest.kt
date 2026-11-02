package com.tochlabs.setlist.feature.share.pdf

import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** English texts, so the tests read like the specs. */
internal val TEXTS =
    ExportTexts(
        stopLabels =
            mapOf(
                StopType.SPEECH to "Speech",
                StopType.BREAK to "Break",
                StopType.TUNING to "Tuning change",
                StopType.ENCORE to "Encore",
            ),
        standardTuning = "Standard",
        none = "none",
        repeated = "Repeated",
        noDuration = "no duration",
        columns = listOf("#", "Title", "Artist", "Key", "BPM", "Time", "Changes"),
        instrumentChange = { "→ $it" },
        tuning = { "Tuning $it" },
        key = { to, from -> "Key $to (orig. $from)" },
        tempo = { to, from -> "$to BPM (orig. $from)" },
        songCount = { "$it songs" },
        totalWithStops = { "Total with stops $it" },
        incomplete = { "Incomplete: $it song without duration" },
        duration = { "${it / 60} min" },
        date = { it.toString() },
        stageFileLabel = "stage",
        listFileLabel = "list",
    )

private var next = 0

private fun song(
    title: String,
    seconds: Int? = 180,
    key: String? = null,
    instrument: String? = null,
    note: String? = null,
    linked: Boolean = false,
    transition: String? = null,
    artist: String? = "Olas",
): ResolvedItem.SongEntry {
    val song = Song(SongId(title), title, artist, originalKey = key, durationSeconds = seconds)
    val item =
        SetlistItem.SongItem(
            SetlistItemId("i${next++}"),
            song.id,
            instrument = instrument,
            note = note,
            linkedToNext = linked,
            transitionText = transition,
        )
    return ResolvedItem.SongEntry(item, song)
}

private fun stop(
    type: StopType,
    seconds: Int,
    note: String? = null,
) = ResolvedItem.StopEntry(SetlistItem.Stop(SetlistItemId("i${next++}"), type, seconds, note))

private fun source(items: List<ResolvedItem>) =
    ExportSource(
        setlist = Setlist(SetlistId("s"), BandId("b"), "Sala Norte", LocalDate(2026, 11, 20), pauseSeconds = 20),
        bandName = "Mareas",
        items = items,
    )

private fun PdfLayout.texts(): List<String> =
    pages.flatMap { page ->
        page.blocks.flatMap { block ->
            block.lines.mapNotNull {
                when (it) {
                    is PdfLine.Text -> it.text
                    is PdfLine.Row -> it.cells.joinToString("|")
                    is PdfLine.Space -> null
                }
            }
        }
    }

// specs/setlist-export.
class PdfLayoutTest {
    @Test
    fun exportStagePdf_nineItemsInOrderWithMarkersAndNotes() {
        val items =
            listOf(
                song("Uno", instrument = "Strato", key = "A"),
                song("Dos", instrument = "Acústica", note = "Capo 2", linked = true, transition = "End on E"),
                song("Tres"),
                stop(StopType.BREAK, 15 * 60, "Drinks and merch"),
                song("Cuatro"),
                song("Cinco"),
                stop(StopType.SPEECH, 60),
                song("Seis"),
                song("Siete"),
            )

        val texts = buildStageLayout(source(items), TEXTS).texts()

        assertEquals("Sala Norte", texts.first())
        assertTrue("Mareas · 2026-11-20" in texts)
        val titles = texts.filter { line -> items.any { line.endsWith(it.title()) } }
        assertEquals(
            listOf(
                "1  Uno",
                "2  Dos",
                "3  Tres",
                "— Break 15:00 —",
                "4  Cuatro",
                "5  Cinco",
                "— Speech 1:00 —",
                "6  Seis",
                "7  Siete",
            ),
            titles,
        )
        assertTrue("A" in texts, "key of the first song")
        assertTrue("→ Acústica" in texts, "instrument change marker")
        assertTrue("Capo 2" in texts, "note")
        assertTrue("↓ End on E" in texts, "link text")
        assertTrue("Drinks and merch" in texts, "stop note")
    }

    @Test
    fun stagePdf_keepsEachItemOnOnePageAndFitsThePage() {
        val items = (1..40).map { song("Song $it", note = "Note $it") }

        val layout = buildStageLayout(source(items), TEXTS)

        assertTrue(layout.pages.size > 1)
        layout.pages.forEach { page -> assertTrue(page.height <= layout.contentHeight) }
        assertEquals(40, layout.texts().count { it.startsWith("Note ") })
    }

    @Test
    fun exportListPdf_oneRowPerItemThenSongCountAndTotalWithStops() {
        val items =
            listOf(
                song("Uno", 180, key = "A", instrument = "Strato"),
                stop(StopType.SPEECH, 60),
                song("Dos", 240, instrument = "Acústica"),
            )

        val texts = buildListLayout(source(items), TEXTS).texts()

        val rows = texts.filter { it.contains("|") }
        assertEquals("#|Title|Artist|Key|BPM|Time|Changes", rows.first())
        assertEquals(
            listOf(
                "1|Uno|Olas|A||3:00|",
                "|Speech||||1:00|",
                "2|Dos|Olas|||4:00|→ Acústica",
            ),
            rows.drop(1),
        )
        // 3:00 + 1:00 + 4:00 + 2 pauses of 20 s = 8:40, shown in minutes by the test texts.
        assertEquals("2 songs · Total with stops 8 min", texts.last())
        assertFalse(texts.any { it.startsWith("Incomplete") })
    }

    @Test
    fun listPdf_incompleteTotal() {
        val texts = buildListLayout(source(listOf(song("Uno", 180), song("Dos", null))), TEXTS).texts()

        assertTrue("2|Dos|Olas|||no duration|" in texts)
        assertEquals("Incomplete: 1 song without duration", texts.last())
    }

    @Test
    fun listPdf_repeatsTheTableHeaderOnEveryPage() {
        val layout = buildListLayout(source((1..120).map { song("Song $it") }), TEXTS)

        assertTrue(layout.pages.size > 1)
        layout.pages.forEach { page ->
            val firstRow =
                page.blocks
                    .flatMap { it.lines }
                    .filterIsInstance<PdfLine.Row>()
                    .first()
            assertTrue(firstRow.header)
            assertTrue(page.height <= layout.contentHeight)
        }
        assertEquals(120, layout.texts().count { it.contains("|Song ") })
    }
}

private fun ResolvedItem.title(): String =
    when (this) {
        is ResolvedItem.SongEntry -> song.title
        is ResolvedItem.StopEntry ->
            TEXTS.stopLabels.getValue(item.type) + " " +
                "${item.durationSeconds / 60}:${(item.durationSeconds % 60).toString().padStart(2, '0')} —"
    }
