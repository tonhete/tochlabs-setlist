package com.tochlabs.setlist.core.data.repository

import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.RepertoireEntity
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.Timestamps
import com.tochlabs.setlist.core.data.db.toEntity
import com.tochlabs.setlist.core.data.db.toModel
import com.tochlabs.setlist.core.data.transfer.FILE_BAND_ID
import com.tochlabs.setlist.core.data.transfer.FILE_SETLIST_ID
import com.tochlabs.setlist.core.data.transfer.fileItemId
import com.tochlabs.setlist.core.data.transfer.fileSongId
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.text.sameSong
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.domain.transfer.SetlistTransferRepository
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SetlistPackage
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId

/** `.setlist` export and import over Room (specs/setlist-sharing, design.md decision 8). */
internal class RoomSetlistTransferRepository(
    private val db: SetlistDatabase,
    private val clock: Clock,
    private val ids: IdGenerator,
) : SetlistTransferRepository {
    private val bands = db.bandDao()
    private val songs = db.songDao()
    private val repertoire = db.repertoireDao()
    private val setlists = db.setlistDao()
    private val items = db.setlistItemDao()

    override suspend fun exportPackage(setlistId: SetlistId): SetlistPackage? {
        val setlist = setlists.get(setlistId.value)?.toModel() ?: return null
        val band = bands.get(setlist.bandId.value)?.toModel()
        val stored = items.getBySetlist(setlistId.value).map { it.toModel() }
        // Each song used, once, in order of first use, with a file-local id.
        val used = stored.filterIsInstance<SetlistItem.SongItem>().map { it.songId }.distinct()
        val fileIds = used.withIndex().associate { (index, id) -> id to fileSongId(index) }
        val usedSongs = used.mapNotNull { id -> songs.get(id.value)?.toModel()?.copy(id = fileIds.getValue(id)) }
        return SetlistPackage(
            bandName = band?.name.orEmpty(),
            setlist = setlist.copy(id = FILE_SETLIST_ID, bandId = FILE_BAND_ID),
            items =
                stored.mapIndexed { index, item ->
                    when (item) {
                        is SetlistItem.SongItem ->
                            item.copy(
                                id = fileItemId(index),
                                songId = fileIds.getValue(item.songId),
                            )
                        is SetlistItem.Stop -> item.copy(id = fileItemId(index))
                    }
                },
            songs = usedSongs,
            exportedAt = clock.now(),
        )
    }

    override suspend fun import(
        pkg: SetlistPackage,
        bandId: BandId,
    ): SetlistId =
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val time = Timestamps(now, now)
            val songIds = matchOrCreateSongs(pkg.songs, bandId, time)
            val setlistId = SetlistId(ids.newId())
            setlists.insert(pkg.setlist.copy(id = setlistId, bandId = bandId).toEntity(time))
            pkg.items.forEachIndexed { position, item ->
                val newItem =
                    when (item) {
                        is SetlistItem.SongItem ->
                            item.copy(id = SetlistItemId(ids.newId()), songId = songIds.getValue(item.songId))
                        is SetlistItem.Stop -> item.copy(id = SetlistItemId(ids.newId()))
                    }
                items.insert(newItem.toEntity(setlistId.value, position, time))
            }
            setlistId
        }

    /** File song id to catalogue song id: an existing match, or a new song added to the band's repertoire. */
    private suspend fun matchOrCreateSongs(
        fileSongs: List<Song>,
        bandId: BandId,
        time: Timestamps,
    ): Map<SongId, SongId> {
        val catalogue = songs.getAll().map { it.toModel() }.toMutableList()
        return fileSongs.associate { song ->
            val existing = catalogue.find { sameSong(it.title, it.artist, song.title, song.artist) }
            val id =
                existing?.id ?: SongId(ids.newId()).also { newId ->
                    val created = song.copy(id = newId)
                    songs.insert(created.toEntity(time))
                    repertoire.insertAll(listOf(RepertoireEntity(bandId.value, newId.value)))
                    catalogue += created
                }
            song.id to id
        }
    }
}
