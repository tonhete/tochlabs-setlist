package com.tochlabs.setlist.core.data.transfer

import com.tochlabs.setlist.core.domain.transfer.SetlistFileResult
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SetlistPackage
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

// specs/setlist-sharing: file content, "Reject invalid files"; design.md decision 8.
internal class JsonSetlistFileCodecTest {
    private val codec = JsonSetlistFileCodec()

    /** A package already using the file-local ids the codec produces when reading. */
    private val pkg =
        SetlistPackage(
            bandName = "Mareas",
            setlist = Setlist(FILE_SETLIST_ID, FILE_BAND_ID, "Concierto Sala Norte", LocalDate(2026, 11, 20), 25),
            items =
                listOf(
                    SetlistItem.SongItem(
                        SetlistItemId("i1"),
                        SongId("s1"),
                        keyOverride = "G",
                        bpmOverride = 128,
                        instrument = "Strato",
                        tuningOverride = "Drop D",
                        note = "Capo 2",
                        linkedToNext = true,
                        transitionText = "End on E",
                    ),
                    SetlistItem.SongItem(SetlistItemId("i2"), SongId("s2")),
                    SetlistItem.Stop(SetlistItemId("i3"), StopType.BREAK, 900, "Drinks and merch"),
                    SetlistItem.SongItem(SetlistItemId("i4"), SongId("s1")),
                ),
            songs =
                listOf(
                    Song(SongId("s1"), "Faro", "Olas", "A", 120, null, 225),
                    Song(SongId("s2"), "Canción de cuna"),
                ),
            exportedAt = Instant.parse("2026-11-14T12:00:00Z"),
        )

    @Test
    fun roundTripKeepsOrderStopsLinksNotesOverridesPauseAndSongs() {
        assertEquals(SetlistFileResult.Valid(pkg), codec.decode(codec.encode(pkg)))
    }

    @Test
    fun theFileIsLanguageIndependentTypesAreStoredAsCodes() {
        val text = codec.encode(pkg)
        assertTrue("\"formatVersion\": \"1.0\"" in text)
        assertTrue("\"break\"" in text)
        assertFalse("BREAK" in text)
    }

    @Test
    fun newerMajorVersionIsRejectedAsNewer() {
        val text = codec.encode(pkg).replace("\"1.0\"", "\"2.0\"")
        assertEquals(SetlistFileResult.NewerVersion, codec.decode(text))
    }

    @Test
    fun newerMinorVersionWithUnknownFieldsIsAccepted() {
        val text =
            codec
                .encode(pkg)
                .replace("\"1.0\"", "\"1.3\"")
                .replace("\"band\":", "\"futureField\": 42, \"band\":")
        assertEquals(SetlistFileResult.Valid(pkg), codec.decode(text))
    }

    @Test
    fun corruptFileIsUnreadable() {
        assertEquals(SetlistFileResult.Unreadable, codec.decode("not json at all"))
        assertEquals(SetlistFileResult.Unreadable, codec.decode(codec.encode(pkg).dropLast(20)))
        assertEquals(SetlistFileResult.Unreadable, codec.decode("{\"hello\": 1}"))
        assertEquals(SetlistFileResult.Unreadable, codec.decode(""))
    }

    @Test
    fun invalidContentIsUnreadable() {
        val missingSong = codec.encode(pkg).replace("\"song\": \"s2\"", "\"song\": \"s9\"")
        val unknownStop = codec.encode(pkg).replace("\"break\"", "\"party\"")
        val blankTitle = codec.encode(pkg).replace("\"Faro\"", "\"  \"")
        val badPause = codec.encode(pkg).replace("\"pauseSeconds\": 25", "\"pauseSeconds\": 7")
        listOf(missingSong, unknownStop, blankTitle, badPause).forEach {
            assertEquals(SetlistFileResult.Unreadable, codec.decode(it))
        }
    }

    @Test
    fun overlongInstrumentAndTuningAreCutTo20Characters() {
        val text = codec.encode(pkg).replace("\"Strato\"", "\"Guitarra acústica de doce cuerdas\"")
        val item = (codec.decode(text) as SetlistFileResult.Valid).pkg.items.first() as SetlistItem.SongItem
        assertEquals("Guitarra acústica de", item.instrument)
    }

    private companion object {
        val FILE_SETLIST_ID = SetlistId("setlist")
        val FILE_BAND_ID = BandId("band")
    }
}
