package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.testing.FakeBandRepository
import com.tochlabs.setlist.core.testing.FakeClock
import com.tochlabs.setlist.core.testing.FakeEventRepository
import com.tochlabs.setlist.core.testing.FakeSetlistRepository
import com.tochlabs.setlist.core.testing.FakeSettingsRepository
import com.tochlabs.setlist.core.testing.FakeSongRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.SequentialIdGenerator
import kotlinx.datetime.LocalDate

internal val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
internal val sabados = Band(BandId("sabados"), "Los Sábados", BandColor(0xFFE65100))

/** FakeClock's today. */
internal val TODAY = LocalDate(2026, 11, 14)

internal fun minutes(
    m: Int,
    s: Int = 0,
) = m * 60 + s

/** One fake database with the repositories the setlist screens use. */
internal class SetlistsTestEnv {
    val store = FakeStore()
    val bands = FakeBandRepository(store)
    val songs = FakeSongRepository(store)
    val setlists = FakeSetlistRepository(store)
    val events = FakeEventRepository(store)
    val settings = FakeSettingsRepository()
    val clock = FakeClock()
    val ids = SequentialIdGenerator()

    fun listViewModel() = SetlistsViewModel(setlists, bands, songs, events, clock)

    fun formViewModel(
        setlistId: SetlistId? = null,
        bandId: BandId? = null,
    ) = SetlistFormViewModel(setlistId, bandId, setlists, bands, settings, ids)

    fun detailViewModel(setlistId: SetlistId) = SetlistDetailViewModel(setlistId, setlists, songs, bands, events, ids)

    fun addSongViewModel(setlistId: SetlistId) = AddSongViewModel(setlistId, setlists, songs, ids)

    suspend fun bothBands() {
        bands.save(mareas)
        bands.save(sabados)
    }

    suspend fun setlist(
        name: String,
        date: LocalDate? = null,
        band: Band = mareas,
        pauseSeconds: Int = 20,
    ): Setlist {
        val setlist = Setlist(SetlistId(name), band.id, name, date, pauseSeconds)
        setlists.save(setlist)
        return setlist
    }

    suspend fun song(
        title: String,
        durationSeconds: Int? = null,
        key: String? = null,
        bpm: Int? = null,
        tuning: String? = null,
        artist: String? = null,
    ): Song {
        val song = Song(SongId(title), title, artist, key, bpm, tuning, durationSeconds)
        songs.save(song)
        return song
    }

    private var nextItem = 1

    suspend fun addSong(
        setlist: Setlist,
        song: Song,
        instrument: String? = null,
        linkedToNext: Boolean = false,
        transitionText: String? = null,
    ): SetlistItem.SongItem {
        val item =
            SetlistItem.SongItem(
                SetlistItemId("item-${nextItem++}"),
                song.id,
                instrument = instrument,
                linkedToNext = linkedToNext,
                transitionText = transitionText,
            )
        setlists.addItem(setlist.id, item)
        return item
    }

    suspend fun addStop(
        setlist: Setlist,
        type: StopType,
        durationSeconds: Int,
    ): SetlistItem.Stop {
        val item = SetlistItem.Stop(SetlistItemId("item-${nextItem++}"), type, durationSeconds)
        setlists.addItem(setlist.id, item)
        return item
    }

    fun linkEvent(setlist: Setlist) {
        store.events.value +=
            Event(
                EventId("event-${setlist.id.value}"),
                setlist.bandId,
                EventType.GIG,
                TODAY,
                "Gig",
                setlistId = setlist.id,
            )
    }

    fun itemsOf(setlist: Setlist): List<SetlistItem> = store.items.value[setlist.id].orEmpty()
}
