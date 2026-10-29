package com.tochlabs.setlist.core.data.db

import com.tochlabs.setlist.core.domain.text.normalizeText
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlin.time.Instant

internal fun BandEntity.toModel() =
    Band(
        id = BandId(id),
        name = name,
        color = BandColor(colorArgb),
        archivedAt = archivedAt?.let(Instant::fromEpochMilliseconds),
    )

internal fun Band.toEntity(
    archivedAt: Long?,
    time: Timestamps,
) = BandEntity(id.value, name, color.argb, archivedAt, time.createdAt, time.updatedAt)

internal fun SongEntity.toModel() =
    Song(
        id = SongId(id),
        title = title,
        artist = artist,
        originalKey = originalKey,
        originalBpm = originalBpm,
        originalTuning = originalTuning,
        durationSeconds = durationSeconds,
    )

internal fun Song.toEntity(time: Timestamps) =
    SongEntity(
        id = id.value,
        title = title,
        normalizedTitle = normalizeText(title),
        artist = artist,
        normalizedArtist = artist?.let(::normalizeText),
        originalKey = originalKey,
        originalBpm = originalBpm,
        originalTuning = originalTuning,
        durationSeconds = durationSeconds,
        createdAt = time.createdAt,
        updatedAt = time.updatedAt,
    )
