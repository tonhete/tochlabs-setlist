package com.tochlabs.setlist.core.data.repository

import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.RepertoireEntity
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.Timestamps
import com.tochlabs.setlist.core.data.db.toEntity
import com.tochlabs.setlist.core.data.db.toModel
import com.tochlabs.setlist.core.domain.repository.DeleteSongResult
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.text.normalizeText
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomSongRepository(
    private val db: SetlistDatabase,
    private val clock: Clock,
) : SongRepository {
    private val dao = db.songDao()
    private val repertoire = db.repertoireDao()

    override fun observeSongs(bandId: BandId?): Flow<List<Song>> {
        val rows = if (bandId == null) dao.observeAll() else dao.observeByBand(bandId.value)
        return rows.map { list -> list.map { it.toModel() } }
    }

    override fun search(query: String): Flow<List<Song>> {
        val needle = normalizeText(query)
        val rows = if (needle.isEmpty()) dao.observeAll() else dao.search("%${needle.escapeLike()}%")
        return rows.map { list -> list.map { it.toModel() } }
    }

    override fun observe(id: SongId): Flow<Song?> = dao.observe(id.value).map { it?.toModel() }

    override suspend fun save(song: Song) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val existing = dao.get(song.id.value)
            val row = song.toEntity(Timestamps(existing?.createdAt ?: now, now))
            if (existing == null) dao.insert(row) else dao.update(row)
        }
    }

    override fun observeBandsOf(songId: SongId): Flow<Set<BandId>> =
        repertoire.observeBandIds(songId.value).map { ids -> ids.map(::BandId).toSet() }

    override suspend fun setBands(
        songId: SongId,
        bandIds: Set<BandId>,
    ) {
        db.withWriteTransaction {
            repertoire.deleteForSong(songId.value)
            repertoire.insertAll(bandIds.map { RepertoireEntity(it.value, songId.value) })
        }
    }

    /** The RESTRICT foreign key also guards this; checking first lets us name the setlists. */
    override suspend fun delete(id: SongId): DeleteSongResult =
        db.withWriteTransaction {
            val using = dao.setlistsUsing(id.value)
            if (using.isNotEmpty()) {
                DeleteSongResult.InUse(using.map { it.toModel() })
            } else {
                dao.delete(id.value)
                DeleteSongResult.Deleted
            }
        }
}

/** Escapes LIKE wildcards; the queries use '\' as escape character. */
private fun String.escapeLike(): String =
    replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
