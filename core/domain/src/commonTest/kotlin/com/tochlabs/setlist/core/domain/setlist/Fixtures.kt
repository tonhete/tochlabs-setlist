package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType

// Small builders so each test reads like its spec scenario.

private var nextId = 0

private fun itemId() = SetlistItemId("item-${nextId++}")

fun minutes(
    m: Int,
    s: Int = 0,
): Int = m * 60 + s

fun song(
    durationSeconds: Int? = null,
    id: String = "song-${nextId++}",
    key: String? = null,
    bpm: Int? = null,
    tuning: String? = null,
): Song =
    Song(
        SongId(id),
        title = id,
        originalKey = key,
        originalBpm = bpm,
        originalTuning = tuning,
        durationSeconds = durationSeconds,
    )

fun songEntry(
    song: Song,
    linkedToNext: Boolean = false,
    keyOverride: String? = null,
    bpmOverride: Int? = null,
    instrument: String? = null,
    tuningOverride: String? = null,
    transitionText: String? = null,
): ResolvedItem.SongEntry =
    ResolvedItem.SongEntry(
        item =
            SetlistItem.SongItem(
                id = itemId(),
                songId = song.id,
                keyOverride = keyOverride,
                bpmOverride = bpmOverride,
                instrument = instrument,
                tuningOverride = tuningOverride,
                linkedToNext = linkedToNext,
                transitionText = transitionText,
            ),
        song = song,
    )

fun songEntry(
    durationSeconds: Int?,
    linkedToNext: Boolean = false,
): ResolvedItem.SongEntry = songEntry(song(durationSeconds), linkedToNext = linkedToNext)

fun stop(
    durationSeconds: Int,
    type: StopType = StopType.BREAK,
): ResolvedItem.StopEntry = ResolvedItem.StopEntry(SetlistItem.Stop(itemId(), type, durationSeconds))
