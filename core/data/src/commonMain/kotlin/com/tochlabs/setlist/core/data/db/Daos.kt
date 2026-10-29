package com.tochlabs.setlist.core.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

// Writes use explicit insert and update. Repositories know whether a row exists, and @Upsert
// detects conflicts from the SQLite error message, which host tests lose. REPLACE is avoided
// because it deletes the row first and would fire ON DELETE CASCADE.

@Dao
internal interface BandDao {
    @Query("SELECT * FROM bands WHERE archivedAt IS NULL ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<BandEntity>>

    @Query("SELECT * FROM bands WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun observeArchived(): Flow<List<BandEntity>>

    @Query("SELECT * FROM bands WHERE id = :id")
    fun observe(id: String): Flow<BandEntity?>

    @Query("SELECT * FROM bands WHERE id = :id")
    suspend fun get(id: String): BandEntity?

    @Insert
    suspend fun insert(band: BandEntity)

    @Update
    suspend fun update(band: BandEntity)

    @Query("UPDATE bands SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchivedAt(
        id: String,
        archivedAt: Long?,
        updatedAt: Long,
    )

    @Query("DELETE FROM bands WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM setlists WHERE bandId = :id")
    suspend fun countSetlists(id: String): Int

    @Query("SELECT COUNT(*) FROM events WHERE bandId = :id")
    suspend fun countEvents(id: String): Int

    @Query("SELECT COUNT(*) FROM repertoire WHERE bandId = :id")
    suspend fun countRepertoire(id: String): Int
}

@Dao
internal interface SongDao {
    @Query("SELECT * FROM songs ORDER BY normalizedTitle")
    fun observeAll(): Flow<List<SongEntity>>

    @Query(
        "SELECT s.* FROM songs s JOIN repertoire r ON r.songId = s.id " +
            "WHERE r.bandId = :bandId ORDER BY s.normalizedTitle",
    )
    fun observeByBand(bandId: String): Flow<List<SongEntity>>

    /** [pattern] is a normalised LIKE pattern such as %cancion%. */
    @Query(
        "SELECT * FROM songs WHERE normalizedTitle LIKE :pattern ESCAPE '\\' " +
            "OR normalizedArtist LIKE :pattern ESCAPE '\\' " +
            "ORDER BY normalizedTitle",
    )
    fun search(pattern: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun observe(id: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun get(id: String): SongEntity?

    @Insert
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        "SELECT DISTINCT s.* FROM setlists s JOIN setlist_items i ON i.setlistId = s.id " +
            "WHERE i.songId = :songId ORDER BY s.name COLLATE NOCASE",
    )
    suspend fun setlistsUsing(songId: String): List<SetlistEntity>
}

@Dao
internal interface RepertoireDao {
    @Query("SELECT bandId FROM repertoire WHERE songId = :songId")
    fun observeBandIds(songId: String): Flow<List<String>>

    @Insert
    suspend fun insertAll(entries: List<RepertoireEntity>)

    @Query("DELETE FROM repertoire WHERE songId = :songId")
    suspend fun deleteForSong(songId: String)
}

@Dao
internal interface SetlistDao {
    @Query(
        "SELECT s.* FROM setlists s JOIN bands b ON b.id = s.bandId " +
            "WHERE b.archivedAt IS NULL ORDER BY s.date IS NULL, s.date, s.name COLLATE NOCASE",
    )
    fun observeActive(): Flow<List<SetlistEntity>>

    @Query(
        "SELECT s.* FROM setlists s JOIN bands b ON b.id = s.bandId " +
            "WHERE b.archivedAt IS NULL AND s.bandId = :bandId " +
            "ORDER BY s.date IS NULL, s.date, s.name COLLATE NOCASE",
    )
    fun observeActiveByBand(bandId: String): Flow<List<SetlistEntity>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    fun observe(id: String): Flow<SetlistEntity?>

    @Query("SELECT * FROM setlists WHERE id = :id")
    suspend fun get(id: String): SetlistEntity?

    @Insert
    suspend fun insert(setlist: SetlistEntity)

    @Update
    suspend fun update(setlist: SetlistEntity)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
internal interface SetlistItemDao {
    @Query("SELECT * FROM setlist_items WHERE setlistId = :setlistId ORDER BY position")
    fun observeBySetlist(setlistId: String): Flow<List<SetlistItemEntity>>

    @Query("SELECT * FROM setlist_items WHERE setlistId = :setlistId ORDER BY position")
    suspend fun getBySetlist(setlistId: String): List<SetlistItemEntity>

    @Query("SELECT * FROM setlist_items WHERE id = :id")
    suspend fun get(id: String): SetlistItemEntity?

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM setlist_items WHERE setlistId = :setlistId")
    suspend fun nextPosition(setlistId: String): Int

    @Insert
    suspend fun insert(item: SetlistItemEntity)

    @Update
    suspend fun update(item: SetlistItemEntity)

    @Query("UPDATE setlist_items SET position = :position WHERE id = :id")
    suspend fun setPosition(
        id: String,
        position: Int,
    )

    @Query("DELETE FROM setlist_items WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
internal interface EventDao {
    @Query(
        "SELECT e.* FROM events e JOIN bands b ON b.id = e.bandId " +
            "WHERE b.archivedAt IS NULL ORDER BY e.date, e.title COLLATE NOCASE",
    )
    fun observeActive(): Flow<List<EventEntity>>

    @Query(
        "SELECT e.* FROM events e JOIN bands b ON b.id = e.bandId " +
            "WHERE b.archivedAt IS NULL AND e.bandId = :bandId ORDER BY e.date, e.title COLLATE NOCASE",
    )
    fun observeActiveByBand(bandId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun observe(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE setlistId = :setlistId")
    fun observeBySetlist(setlistId: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun get(id: String): EventEntity?

    @Insert
    suspend fun insert(event: EventEntity)

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
internal interface EventMilestoneDao {
    @Query("SELECT * FROM event_milestones WHERE eventId = :eventId ORDER BY startTime")
    fun observeByEvent(eventId: String): Flow<List<EventMilestoneEntity>>

    @Query("SELECT * FROM event_milestones WHERE id = :id")
    suspend fun get(id: String): EventMilestoneEntity?

    @Insert
    suspend fun insert(milestone: EventMilestoneEntity)

    @Update
    suspend fun update(milestone: EventMilestoneEntity)

    @Query("DELETE FROM event_milestones WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
internal interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 0")
    fun observe(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 0")
    suspend fun get(): AppSettingsEntity?

    @Insert
    suspend fun insert(settings: AppSettingsEntity)

    @Update
    suspend fun update(settings: AppSettingsEntity)
}
