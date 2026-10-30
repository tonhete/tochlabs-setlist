package com.tochlabs.setlist.feature.bands

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.testing.FakeBandRepository
import com.tochlabs.setlist.core.testing.FakeSetlistRepository
import com.tochlabs.setlist.core.testing.FakeSongRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.SequentialIdGenerator
import kotlinx.datetime.LocalDate

/** One fake database with the repositories the bands screens use. */
internal class BandsTestEnv {
    val store = FakeStore()
    val bands = FakeBandRepository(store)
    val songs = FakeSongRepository(store)
    val setlists = FakeSetlistRepository(store)
    val ids = SequentialIdGenerator()

    fun listViewModel() = BandsViewModel(bands, songs, setlists)

    fun historyViewModel() = BandHistoryViewModel(bands)

    fun formViewModel(bandId: BandId? = null) = BandFormViewModel(bandId, bands, ids)

    /** "Mareas" with one song in its repertoire, one setlist and one event. */
    suspend fun mareasWithData(): Band {
        val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
        val song = Song(SongId("faro"), "Faro")
        val setlist = Setlist(SetlistId("sala-norte"), mareas.id, "Sala Norte", pauseSeconds = 20)
        bands.save(mareas)
        songs.save(song)
        songs.setBands(song.id, setOf(mareas.id))
        setlists.save(setlist)
        store.events.value =
            listOf(
                Event(
                    EventId("e1"),
                    mareas.id,
                    EventType.GIG,
                    LocalDate(2026, 11, 20),
                    "Concierto",
                    setlistId = setlist.id,
                ),
            )
        return mareas
    }
}
