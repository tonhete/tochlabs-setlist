package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.StopType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Scenarios from specs/setlists.
internal class SetlistRepositoryTest : RepositoryTestBase() {
    private val mareas = band("Mareas")
    private val salaNorte = setlist("Concierto Sala Norte", mareas, LocalDate(2026, 11, 20))

    private suspend fun prepare() {
        bands.save(mareas)
        listOf("Uno", "Dos", "Tres").forEach { songs.save(song(it)) }
        setlists.save(salaNorte)
    }

    private suspend fun itemIds() = setlists.observeItems(salaNorte.id).first().map { it.id.value }

    @Test
    fun createASetlist() =
        runTest {
            prepare()
            assertEquals(listOf(salaNorte), setlists.observeSetlists(mareas.id).first())
        }

    @Test
    fun setlistWithoutDate() =
        runTest {
            prepare()
            val undated = setlist("Ensayo", mareas)
            setlists.save(undated)
            assertNull(setlists.observe(undated.id).first()?.date)
        }

    @Test
    fun filterSetlistsByBand() =
        runTest {
            prepare()
            val other = band("Otra")
            bands.save(other)
            setlists.save(setlist("Bolo", other))
            assertEquals(listOf(salaNorte), setlists.observeSetlists(mareas.id).first())
            assertEquals(2, setlists.observeSetlists().first().size)
        }

    @Test
    fun addASongAndAStopAppendThem() =
        runTest {
            prepare()
            setlists.addItem(salaNorte.id, songItem("a", song("Uno")))
            setlists.addItem(salaNorte.id, SetlistItem.Stop(SetlistItemId("b"), StopType.BREAK, 15 * 60))
            setlists.addItem(salaNorte.id, songItem("c", song("Dos")))

            val items = setlists.observeItems(salaNorte.id).first()
            assertEquals(listOf("a", "b", "c"), items.map { it.id.value })
            assertEquals(SetlistItem.Stop(SetlistItemId("b"), StopType.BREAK, 900), items[1])
        }

    @Test
    fun overridesAndLinksAreStored() =
        runTest {
            prepare()
            val item =
                SetlistItem.SongItem(
                    id = SetlistItemId("a"),
                    songId = song("Uno").id,
                    keyOverride = "G",
                    bpmOverride = 140,
                    instrument = "Strato",
                    tuningOverride = "Eb",
                    note = "Capo 2",
                    linkedToNext = true,
                    transitionText = "End on E",
                )
            setlists.addItem(salaNorte.id, item)
            assertEquals(listOf(item), setlists.observeItems(salaNorte.id).first())
        }

    @Test
    fun updateAnItemKeepsItsPosition() =
        runTest {
            prepare()
            setlists.addItem(salaNorte.id, songItem("a", song("Uno")))
            setlists.addItem(salaNorte.id, songItem("b", song("Dos")))

            setlists.updateItem(songItem("a", song("Uno")).copy(keyOverride = "G"))

            val items = setlists.observeItems(salaNorte.id).first()
            assertEquals(listOf("a", "b"), items.map { it.id.value })
            assertEquals("G", (items[0] as SetlistItem.SongItem).keyOverride)
        }

    @Test
    fun reorderByDragging() =
        runTest {
            prepare()
            listOf("a" to "Uno", "b" to "Dos", "c" to "Tres").forEach { (id, title) ->
                setlists.addItem(salaNorte.id, songItem(id, song(title)))
            }

            setlists.reorder(salaNorte.id, listOf("c", "a", "b").map(::SetlistItemId))

            assertEquals(listOf("c", "a", "b"), itemIds())
            assertEquals(listOf(0, 1, 2), db.setlistItemDao().getBySetlist(salaNorte.id.value).map { it.position })
        }

    @Test
    fun reorderMustListEveryItem() =
        runTest {
            prepare()
            setlists.addItem(salaNorte.id, songItem("a", song("Uno")))
            setlists.addItem(salaNorte.id, songItem("b", song("Dos")))

            assertFailsWith<IllegalArgumentException> { setlists.reorder(salaNorte.id, listOf(SetlistItemId("a"))) }
            assertEquals(listOf("a", "b"), itemIds())
        }

    @Test
    fun removeASongFromASetlist() =
        runTest {
            prepare()
            listOf("a" to "Uno", "b" to "Dos", "c" to "Tres").forEach { (id, title) ->
                setlists.addItem(salaNorte.id, songItem(id, song(title)))
            }

            setlists.removeItem(SetlistItemId("b"))

            assertEquals(listOf("a", "c"), itemIds())
            assertEquals(listOf(0, 1), db.setlistItemDao().getBySetlist(salaNorte.id.value).map { it.position })
            assertNotNull(songs.observe(song("Dos").id).first())
        }

    @Test
    fun confirmedDeletionOfASetlistLinkedToAnEvent() =
        runTest {
            prepare()
            setlists.addItem(salaNorte.id, songItem("a", song("Uno")))
            val gig = event("Concierto", mareas, salaNorte)
            events.save(gig)

            setlists.delete(salaNorte.id)

            assertNull(setlists.observe(salaNorte.id).first())
            assertEquals(gig.copy(setlistId = null), events.observe(gig.id).first())
            assertEquals(3, songs.observeSongs().first().size)
        }
}
