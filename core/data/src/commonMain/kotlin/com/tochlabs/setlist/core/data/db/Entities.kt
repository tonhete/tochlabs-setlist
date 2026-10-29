package com.tochlabs.setlist.core.data.db

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.model.MilestoneKind
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// Tables from design.md (decision 3). Ids are UUID strings, timestamps epoch milliseconds,
// dates and times ISO text. Entities stay internal: mappers turn them into core:model types.

@Entity(tableName = "bands", indices = [Index("archivedAt")])
internal data class BandEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorArgb: Long,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "songs", indices = [Index("normalizedTitle")])
internal data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** Title without case and accents, for the catalogue search. */
    val normalizedTitle: String,
    val artist: String?,
    val normalizedArtist: String?,
    val originalKey: String?,
    val originalBpm: Int?,
    val originalTuning: String?,
    val durationSeconds: Int?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "repertoire",
    primaryKeys = ["bandId", "songId"],
    foreignKeys = [
        ForeignKey(BandEntity::class, ["id"], ["bandId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(SongEntity::class, ["id"], ["songId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("songId")],
)
internal data class RepertoireEntity(
    val bandId: String,
    val songId: String,
)

@Entity(
    tableName = "setlists",
    foreignKeys = [ForeignKey(BandEntity::class, ["id"], ["bandId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("bandId", "date")],
)
internal data class SetlistEntity(
    @PrimaryKey val id: String,
    val bandId: String,
    val name: String,
    val date: LocalDate?,
    val pauseSeconds: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

internal enum class ItemKind { SONG, STOP }

/** Songs and stops share one table so ordering works on one sequence (design.md). */
@Entity(
    tableName = "setlist_items",
    foreignKeys = [
        ForeignKey(SetlistEntity::class, ["id"], ["setlistId"], onDelete = ForeignKey.CASCADE),
        // RESTRICT: a song used by a setlist cannot be deleted.
        ForeignKey(SongEntity::class, ["id"], ["songId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("setlistId", "position"), Index("songId")],
)
internal data class SetlistItemEntity(
    @PrimaryKey val id: String,
    val setlistId: String,
    val position: Int,
    val kind: ItemKind,
    val songId: String?,
    val keyOverride: String?,
    val bpmOverride: Int?,
    val instrument: String?,
    val tuningOverride: String?,
    val note: String?,
    val linkedToNext: Boolean,
    val transitionText: String?,
    val stopType: StopType?,
    val durationSeconds: Int?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(BandEntity::class, ["id"], ["bandId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(SetlistEntity::class, ["id"], ["setlistId"], onDelete = ForeignKey.SET_NULL),
    ],
    // Unique: at most one event per setlist (SQLite allows many nulls).
    indices = [Index("bandId", "date"), Index("setlistId", unique = true)],
)
internal data class EventEntity(
    @PrimaryKey val id: String,
    val bandId: String,
    val type: EventType,
    val date: LocalDate,
    val title: String,
    val venue: String?,
    val address: String?,
    val parking: String?,
    val contact: String?,
    val setlistId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "event_milestones",
    foreignKeys = [ForeignKey(EventEntity::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId", "startTime")],
)
internal data class EventMilestoneEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val kind: MilestoneKind,
    val customLabel: String?,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Single row, id always [AppSettingsEntity.ROW_ID]. */
@Entity(tableName = "app_settings")
internal data class AppSettingsEntity(
    @PrimaryKey val id: Int = ROW_ID,
    val language: AppLanguage,
    val defaultPauseSeconds: Int,
) {
    companion object {
        const val ROW_ID = 0
    }
}
