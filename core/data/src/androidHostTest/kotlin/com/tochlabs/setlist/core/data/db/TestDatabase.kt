package com.tochlabs.setlist.core.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.model.MilestoneKind
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Fresh in-memory database, the same bundled SQLite as the app. */
internal fun inMemoryDatabase(): SetlistDatabase =
    Room
        .inMemoryDatabaseBuilder { SetlistDatabaseConstructor.initialize() }
        .setDriver(BundledSQLiteDriver())
        .build()

// Row builders with neutral defaults.

internal fun bandRow(
    id: String,
    name: String = id,
    archivedAt: Long? = null,
) = BandEntity(id, name, colorArgb = 0xFF000000, archivedAt = archivedAt, createdAt = 0, updatedAt = 0)

internal fun songRow(
    id: String,
    title: String = id,
) = SongEntity(id, title, title.lowercase(), null, null, null, null, null, null, 0, 0)

internal fun setlistRow(
    id: String,
    bandId: String,
    date: LocalDate? = null,
) = SetlistEntity(id, bandId, id, date, pauseSeconds = 20, createdAt = 0, updatedAt = 0)

internal fun songItemRow(
    id: String,
    setlistId: String,
    songId: String,
    position: Int,
) = SetlistItemEntity(
    id = id,
    setlistId = setlistId,
    position = position,
    kind = ItemKind.SONG,
    songId = songId,
    keyOverride = null,
    bpmOverride = null,
    instrument = null,
    tuningOverride = null,
    note = null,
    linkedToNext = false,
    transitionText = null,
    stopType = null,
    durationSeconds = null,
    createdAt = 0,
    updatedAt = 0,
)

internal fun stopRow(
    id: String,
    setlistId: String,
    position: Int,
) = songItemRow(id, setlistId, songId = "", position = position)
    .copy(kind = ItemKind.STOP, songId = null, stopType = StopType.BREAK, durationSeconds = 60)

internal fun eventRow(
    id: String,
    bandId: String,
    setlistId: String? = null,
) = EventEntity(
    id = id,
    bandId = bandId,
    type = EventType.GIG,
    date = LocalDate(2026, 11, 20),
    title = id,
    venue = null,
    address = null,
    parking = null,
    contact = null,
    setlistId = setlistId,
    createdAt = 0,
    updatedAt = 0,
)

internal fun milestoneRow(
    id: String,
    eventId: String,
    start: LocalTime,
) = EventMilestoneEntity(id, eventId, MilestoneKind.SOUNDCHECK, null, start, null, 0, 0)
