package com.tochlabs.setlist.core.domain.repository

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    /** Catalogue by title; with a band, only that band's repertoire. */
    fun observeSongs(bandId: BandId? = null): Flow<List<Song>>

    /** Search by title and artist, ignoring case and accents. */
    fun search(query: String): Flow<List<Song>>

    fun observe(id: SongId): Flow<Song?>

    /** Inserts or updates. */
    suspend fun save(song: Song)

    fun observeBandsOf(songId: SongId): Flow<Set<BandId>>

    /** Replaces the bands that play the song. */
    suspend fun setBands(
        songId: SongId,
        bandIds: Set<BandId>,
    )

    /** Deletes the song only when no setlist uses it. */
    suspend fun delete(id: SongId): DeleteSongResult
}

sealed interface DeleteSongResult {
    data object Deleted : DeleteSongResult

    data class InUse(
        val setlists: List<Setlist>,
    ) : DeleteSongResult
}
