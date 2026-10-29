package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.StopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Scenarios from specs/setlist-duration.
class DurationTest {
    @Test
    fun musicOnlySongsOnly() {
        val items = listOf(songEntry(minutes(3)), songEntry(minutes(4)), stop(minutes(10)), songEntry(minutes(5)))
        assertEquals(minutes(12).toLong(), calculateDuration(items, pauseSeconds = 20).musicSeconds)
    }

    @Test
    fun emptySetlist() {
        val summary = calculateDuration(emptyList(), pauseSeconds = 20)
        assertEquals(0L, summary.musicSeconds)
        assertEquals(0L, summary.totalWithStopsSeconds)
        assertEquals(0, summary.pauseCount)
    }

    @Test
    fun songsStopAndPauses() {
        val items =
            listOf(
                songEntry(minutes(3)),
                songEntry(minutes(4)),
                stop(minutes(1), StopType.SPEECH),
                songEntry(minutes(5)),
            )
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(3, summary.pauseCount)
        assertEquals(minutes(14).toLong(), summary.totalWithStopsSeconds)
    }

    @Test
    fun pauseOnBothSidesOfAStop() {
        val items = listOf(songEntry(minutes(3)), stop(minutes(1), StopType.SPEECH), songEntry(minutes(4)))
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(2, summary.pauseCount)
        assertEquals(minutes(8, 40).toLong(), summary.totalWithStopsSeconds)
    }

    @Test
    fun singleItem() {
        val summary = calculateDuration(listOf(songEntry(minutes(3))), pauseSeconds = 20)
        assertEquals(0, summary.pauseCount)
        assertEquals(minutes(3).toLong(), summary.totalWithStopsSeconds)
    }

    @Test
    fun linkedPair() {
        val items = listOf(songEntry(minutes(3), linkedToNext = true), songEntry(minutes(4)))
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(0, summary.pauseCount)
        assertEquals(1, summary.linkedPairs)
        assertEquals(minutes(7).toLong(), summary.totalWithStopsSeconds)
    }

    @Test
    fun linkIgnoredWhenAStopFollows() {
        val items = listOf(songEntry(minutes(3), linkedToNext = true), stop(minutes(1)), songEntry(minutes(4)))
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(2, summary.pauseCount)
        assertEquals(0, summary.linkedPairs)
    }

    @Test
    fun linkOnTheLastItemHasNoEffect() {
        val items = listOf(songEntry(minutes(3)), songEntry(minutes(4), linkedToNext = true))
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(1, summary.pauseCount)
        assertEquals(0, summary.linkedPairs)
    }

    @Test
    fun changeThePauseRecalculatesTheTotal() {
        val items = listOf(songEntry(minutes(3)), songEntry(minutes(4)))
        assertEquals(minutes(7, 25).toLong(), calculateDuration(items, pauseSeconds = 25).totalWithStopsSeconds)
    }

    @Test
    fun oneSongWithoutDuration() {
        val items = listOf(songEntry(minutes(3)), songEntry(null), songEntry(minutes(4)))
        val summary = calculateDuration(items, pauseSeconds = 0)
        assertEquals(minutes(7).toLong(), summary.musicSeconds)
        assertEquals(1, summary.songsWithoutDuration)
        assertFalse(summary.isComplete)
    }

    @Test
    fun everyDurationKnown() {
        val summary = calculateDuration(listOf(songEntry(minutes(3)), songEntry(minutes(4))), pauseSeconds = 20)
        assertTrue(summary.isComplete)
    }

    @Test
    fun switchToMusicOnlyStatesTheTimeLeftOut() {
        val items = listOf(songEntry(minutes(3)), stop(minutes(1)), songEntry(minutes(4)))
        val summary = calculateDuration(items, pauseSeconds = 20)
        assertEquals(minutes(1).toLong(), summary.stopsSeconds)
        assertEquals(40L, summary.pausesSeconds)
        assertEquals(minutes(1, 40).toLong(), summary.leftOutOfMusicOnlySeconds)
    }

    @Test
    fun fasterTempoOverrideKeepsTheDuration() {
        val original = song(durationSeconds = minutes(3), bpm = 100)
        val items = listOf(songEntry(original, bpmOverride = 140, keyOverride = "G"))
        assertEquals(minutes(3).toLong(), calculateDuration(items, pauseSeconds = 20).musicSeconds)
    }
}
