package com.tochlabs.setlist.core.data.db

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SongId

internal fun SetlistEntity.toModel() =
    Setlist(
        id = SetlistId(id),
        bandId = BandId(bandId),
        name = name,
        date = date,
        pauseSeconds = pauseSeconds,
    )

internal fun Setlist.toEntity(time: Timestamps) =
    SetlistEntity(id.value, bandId.value, name, date, pauseSeconds, time.createdAt, time.updatedAt)

/** Rows that break the song/stop rules are rejected: they can only come from a bug. */

internal fun SetlistItemEntity.toModel(): SetlistItem =
    when (kind) {
        ItemKind.SONG ->
            SetlistItem.SongItem(
                id = SetlistItemId(id),
                songId = SongId(checkNotNull(songId) { "Song item $id has no song" }),
                keyOverride = keyOverride,
                bpmOverride = bpmOverride,
                instrument = instrument,
                tuningOverride = tuningOverride,
                note = note,
                linkedToNext = linkedToNext,
                transitionText = transitionText,
            )
        ItemKind.STOP ->
            SetlistItem.Stop(
                id = SetlistItemId(id),
                type = checkNotNull(stopType) { "Stop $id has no type" },
                durationSeconds = checkNotNull(durationSeconds) { "Stop $id has no duration" },
                note = note,
            )
    }

internal fun SetlistItem.toEntity(
    setlistId: String,
    position: Int,
    time: Timestamps,
): SetlistItemEntity {
    val song = this as? SetlistItem.SongItem
    val stop = this as? SetlistItem.Stop
    return SetlistItemEntity(
        id = id.value,
        setlistId = setlistId,
        position = position,
        kind = if (song != null) ItemKind.SONG else ItemKind.STOP,
        songId = song?.songId?.value,
        keyOverride = song?.keyOverride,
        bpmOverride = song?.bpmOverride,
        instrument = song?.instrument,
        tuningOverride = song?.tuningOverride,
        note = note,
        linkedToNext = song?.linkedToNext ?: false,
        transitionText = song?.transitionText,
        stopType = stop?.type,
        durationSeconds = stop?.durationSeconds,
        createdAt = time.createdAt,
        updatedAt = time.updatedAt,
    )
}
