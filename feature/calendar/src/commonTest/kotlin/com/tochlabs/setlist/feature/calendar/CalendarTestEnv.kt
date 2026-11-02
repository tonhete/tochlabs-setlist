package com.tochlabs.setlist.feature.calendar

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
import com.tochlabs.setlist.core.testing.FakeSongRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.SequentialIdGenerator
import kotlinx.datetime.LocalDate

internal val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
internal val sabados = Band(BandId("sabados"), "Los Sábados", BandColor(0xFFE65100))

/** FakeClock's today: Saturday 14 Nov 2026. */
internal val TODAY = LocalDate(2026, 11, 14)

/** One fake database with the repositories the calendar screens use. */
internal class CalendarTestEnv {
    val store = FakeStore()
    val bands = FakeBandRepository(store)
    val events = FakeEventRepository(store)
    val setlists = FakeSetlistRepository(store)
    val songs = FakeSongRepository(store)
    val ids = SequentialIdGenerator()
    private val clock = FakeClock()

    fun calendarViewModel() = CalendarViewModel(events, bands, clock)

    fun formViewModel(
        eventId: EventId? = null,
        date: LocalDate? = null,
        bandId: BandId? = null,
    ) = EventFormViewModel(eventId, date, bandId, events, bands, ids)

    fun detailViewModel(eventId: EventId) = EventDetailViewModel(eventId, events, bands, setlists, songs, ids)

    suspend fun bothBands() {
        bands.save(mareas)
        bands.save(sabados)
    }

    suspend fun event(
        title: String,
        date: LocalDate,
        band: Band = mareas,
        type: EventType = EventType.GIG,
        setlistId: SetlistId? = null,
        parking: String? = null,
        contact: String? = null,
    ): Event {
        val event =
            Event(
                EventId(title),
                band.id,
                type,
                date,
                title,
                parking = parking,
                contact = contact,
                setlistId = setlistId,
            )
        events.save(event)
        return event
    }

    /** A setlist with songs of 3:00 and 4:00 and a 1:00 speech between them, pause 20 s: 8:40 with stops. */
    suspend fun setlist(
        name: String,
        band: Band = mareas,
    ): Setlist {
        val setlist = Setlist(SetlistId(name), band.id, name, pauseSeconds = 20)
        setlists.save(setlist)
        val uno = Song(SongId("$name-1"), "Uno", durationSeconds = 180)
        val dos = Song(SongId("$name-2"), "Dos", durationSeconds = 240)
        songs.save(uno)
        songs.save(dos)
        setlists.addItem(setlist.id, SetlistItem.SongItem(SetlistItemId("$name-a"), uno.id))
        setlists.addItem(setlist.id, SetlistItem.Stop(SetlistItemId("$name-b"), StopType.SPEECH, 60))
        setlists.addItem(setlist.id, SetlistItem.SongItem(SetlistItemId("$name-c"), dos.id))
        return setlist
    }
}
