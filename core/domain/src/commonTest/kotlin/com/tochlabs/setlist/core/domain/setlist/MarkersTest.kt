package com.tochlabs.setlist.core.domain.setlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Scenarios from specs/setlists: change markers, links and repeated songs.
class MarkersTest {
    @Test
    fun instrumentChange() {
        val items =
            listOf(
                songEntry(song(), instrument = "Strato"),
                songEntry(song(), instrument = "Strato"),
                songEntry(song(), instrument = "Acústica"),
            )
        val markers = computeMarkers(items)
        assertNull(markers[1]?.instrumentChangeTo)
        assertEquals("Acústica", markers[2]?.instrumentChangeTo)
    }

    @Test
    fun instrumentIsComparedWithThePreviousSongSkippingStops() {
        val items =
            listOf(songEntry(song(), instrument = "Strato"), stop(60), songEntry(song(), instrument = "Acústica"))
        assertEquals("Acústica", computeMarkers(items)[2]?.instrumentChangeTo)
    }

    @Test
    fun tuningChange() {
        val items = listOf(songEntry(song(tuning = "Drop D")), songEntry(song(), tuningOverride = "Eb"))
        assertEquals(Change<String?>("Drop D", "Eb"), computeMarkers(items)[1]?.tuningChange)
    }

    @Test
    fun sameTuningIsNotAChange() {
        val items = listOf(songEntry(song(tuning = "Drop D")), songEntry(song(tuning = "drop d")))
        assertNull(computeMarkers(items)[1]?.tuningChange)
    }

    @Test
    fun firstSongIsComparedAgainstStandardTuning() {
        assertEquals(
            Change<String?>(null, "Drop D"),
            computeMarkers(listOf(songEntry(song(tuning = "Drop D"))))[0]?.tuningChange,
        )
        assertNull(computeMarkers(listOf(songEntry(song())))[0]?.tuningChange)
    }

    @Test
    fun keyDiffersFromOriginal() {
        val markers = computeMarkers(listOf(songEntry(song(key = "A"), keyOverride = "G")))
        assertEquals(Change<String?>("A", "G"), markers[0]?.keyChange)
    }

    @Test
    fun keyOverrideEqualToOriginalIsNotMarked() {
        assertNull(computeMarkers(listOf(songEntry(song(key = "A"), keyOverride = "A")))[0]?.keyChange)
    }

    @Test
    fun tempoDiffersFromOriginal() {
        val markers = computeMarkers(listOf(songEntry(song(bpm = 100), bpmOverride = 110)))
        assertEquals(Change<Int?>(100, 110), markers[0]?.tempoChange)
    }

    @Test
    fun overrideTheKeyShowsSetlistKeyAndOriginal() {
        val entry = songEntry(song(key = "A"), keyOverride = "G")
        assertEquals("G", entry.effectiveKey)
        assertEquals("A", entry.song.originalKey)
    }

    @Test
    fun itemWithoutOverrideUsesTheOriginalValues() {
        val entry = songEntry(song(key = "A", bpm = 100, tuning = "Drop D"))
        assertEquals("A", entry.effectiveKey)
        assertEquals(100, entry.effectiveBpm)
        assertEquals("Drop D", entry.effectiveTuning)
    }

    @Test
    fun linkTwoConsecutiveSongs() {
        val items =
            listOf(
                songEntry(song(), linkedToNext = true, transitionText = "End on A and drums come in"),
                songEntry(song()),
            )
        val markers = computeMarkers(items)
        assertTrue(markers[0]!!.isLinkedToNext)
        assertTrue(markers[1]!!.isLinkedFromPrevious)
        assertEquals("End on A and drums come in", markers[0]?.transitionText)
    }

    @Test
    fun linkBrokenByAStop() {
        val items =
            listOf(songEntry(song(), linkedToNext = true, transitionText = "End on E"), stop(60), songEntry(song()))
        val markers = computeMarkers(items)
        assertFalse(markers[0]!!.isLinkedToNext)
        assertNull(markers[0]?.transitionText)
        assertFalse(markers[2]!!.isLinkedFromPrevious)
    }

    @Test
    fun repeatedMarker() {
        val repeated = song()
        val markers = computeMarkers(listOf(songEntry(repeated), songEntry(song()), songEntry(repeated)))
        assertFalse(markers[0]!!.isRepeated)
        assertTrue(markers[2]!!.isRepeated)
    }

    @Test
    fun stopsHaveNoMarkers() {
        assertNull(computeMarkers(listOf(songEntry(song()), stop(60)))[1])
    }
}
