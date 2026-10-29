package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.data.db.inMemoryDatabase
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
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest

/** One in-memory database and every repository on top of it. */
internal abstract class RepositoryTestBase {
    protected val clock = FakeClock()
    protected val db = inMemoryDatabase()
    protected val bands = RoomBandRepository(db, clock)
    protected val songs = RoomSongRepository(db, clock)
    protected val setlists = RoomSetlistRepository(db, clock)
    protected val events = RoomEventRepository(db, clock)

    @AfterTest
    fun closeDatabase() = db.close()

    protected fun band(name: String) = Band(BandId(name), name, BandColor(0xFF3366CC))

    protected fun song(
        title: String,
        artist: String? = null,
    ) = Song(SongId(title), title, artist = artist)

    protected fun setlist(
        name: String,
        band: Band,
        date: LocalDate? = null,
    ) = Setlist(SetlistId(name), band.id, name, date, pauseSeconds = 20)

    protected fun songItem(
        id: String,
        song: Song,
    ) = SetlistItem.SongItem(SetlistItemId(id), song.id)

    protected fun event(
        title: String,
        band: Band,
        setlist: Setlist? = null,
    ) = Event(EventId(title), band.id, EventType.GIG, LocalDate(2026, 11, 20), title, setlistId = setlist?.id)
}
