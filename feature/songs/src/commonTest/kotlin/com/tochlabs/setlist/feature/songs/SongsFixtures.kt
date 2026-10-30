package com.tochlabs.setlist.feature.songs

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.testing.FakeBandRepository
import com.tochlabs.setlist.core.testing.FakeSetlistRepository
import com.tochlabs.setlist.core.testing.FakeSongRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.SequentialIdGenerator

internal val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
internal val sabados = Band(BandId("sabados"), "Los Sábados", BandColor(0xFFE65100))

/** One fake database with the repositories the song screens use. */
internal class SongsTestEnv {
    val store = FakeStore()
    val bands = FakeBandRepository(store)
    val songs = FakeSongRepository(store)
    val setlists = FakeSetlistRepository(store)
    val ids = SequentialIdGenerator()

    fun listViewModel() = SongsViewModel(songs, bands)

    fun formViewModel(songId: SongId? = null) = SongFormViewModel(songId, songs, bands, ids)

    suspend fun bothBands() {
        bands.save(mareas)
        bands.save(sabados)
    }

    suspend fun song(
        id: String,
        title: String,
        artist: String? = null,
        durationSeconds: Int? = null,
        bands: Set<BandId> = emptySet(),
    ): Song {
        val song = Song(SongId(id), title, artist = artist, durationSeconds = durationSeconds)
        songs.save(song)
        songs.setBands(song.id, bands)
        return song
    }

    suspend fun setlistUsing(
        name: String,
        song: Song,
    ) {
        val setlist = Setlist(SetlistId(name), mareas.id, name, pauseSeconds = 20)
        setlists.save(setlist)
        setlists.addItem(setlist.id, SetlistItem.SongItem(SetlistItemId("$name-1"), song.id))
    }
}
