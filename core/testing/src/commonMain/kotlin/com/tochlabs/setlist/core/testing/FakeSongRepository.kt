package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.repository.DeleteSongResult
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.text.matchesSearch
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSongRepository(
    private val store: FakeStore,
) : SongRepository {
    override fun observeSongs(bandId: BandId?): Flow<List<Song>> =
        combine(store.songs, store.repertoire) { songs, repertoire ->
            songs
                .filter { song -> bandId == null || (bandId to song.id) in repertoire }
                .sortedBy { it.title.lowercase() }
        }

    override fun search(query: String): Flow<List<Song>> =
        store.songs.map { songs ->
            songs.filter { matchesSearch(query, it.title, it.artist) }.sortedBy { it.title.lowercase() }
        }

    override fun observe(id: SongId): Flow<Song?> = store.songs.map { list -> list.find { it.id == id } }

    override suspend fun save(song: Song) = store.songs.upsert(song) { it.id == song.id }

    override fun observeBandsOf(songId: SongId): Flow<Set<BandId>> =
        store.repertoire.map { set -> set.filter { it.second == songId }.map { it.first }.toSet() }

    override suspend fun setBands(
        songId: SongId,
        bandIds: Set<BandId>,
    ) = store.repertoire.update { set ->
        set.filterNot { it.second == songId }.toSet() + bandIds.map { it to songId }
    }

    /** The real database refuses the deletion through a RESTRICT foreign key. */
    override suspend fun delete(id: SongId): DeleteSongResult {
        val usedIn =
            store.items.value
                .filterValues { items -> items.any { it is SetlistItem.SongItem && it.songId == id } }
                .keys
        if (usedIn.isNotEmpty()) {
            return DeleteSongResult.InUse(
                store.setlists.value
                    .filter { it.id in usedIn }
                    .sortedBy { it.name },
            )
        }
        store.songs.update { list -> list.filterNot { it.id == id } }
        store.repertoire.update { set -> set.filterNot { it.second == id }.toSet() }
        return DeleteSongResult.Deleted
    }
}
