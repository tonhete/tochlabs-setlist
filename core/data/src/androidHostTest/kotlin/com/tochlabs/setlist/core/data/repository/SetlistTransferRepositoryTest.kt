package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.data.UuidIdGenerator
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlist-sharing: export content, "Import a .setlist file", "No duplicate songs on import".
internal class SetlistTransferRepositoryTest : RepositoryTestBase() {
    private val transfer = RoomSetlistTransferRepository(db, clock, UuidIdGenerator)
    private val mareas = band("Mareas")
    private val sabados = band("Los Sábados")
    private val faro = Song(SongId("faro"), "Faro", "Olas", "A", 120, "Drop D", 225)
    private val cuna = Song(SongId("cuna"), "Canción de cuna", durationSeconds = 180)
    private val sala = setlist("Concierto Sala Norte", mareas, LocalDate(2026, 11, 20)).copy(pauseSeconds = 25)

    private suspend fun prepareSender() {
        bands.save(mareas)
        bands.save(sabados)
        songs.save(faro)
        songs.save(cuna)
        setlists.save(sala)
        setlists.addItem(
            sala.id,
            SetlistItem.SongItem(
                SetlistItemId("a"),
                faro.id,
                keyOverride = "G",
                linkedToNext = true,
                transitionText = "End on E",
            ),
        )
        setlists.addItem(sala.id, SetlistItem.SongItem(SetlistItemId("b"), cuna.id, note = "Slow"))
        setlists.addItem(sala.id, SetlistItem.Stop(SetlistItemId("c"), StopType.BREAK, 900, "Drinks"))
        setlists.addItem(sala.id, SetlistItem.SongItem(SetlistItemId("d"), faro.id))
    }

    @Test
    fun exportAFileContainsTheSetlistItsItemsAndEachSongOnce() =
        runTest {
            prepareSender()

            val pkg = transfer.exportPackage(sala.id)!!

            assertEquals("Mareas", pkg.bandName)
            assertEquals("Concierto Sala Norte", pkg.setlist.name)
            assertEquals(25, pkg.setlist.pauseSeconds)
            assertEquals(3, pkg.songCount)
            assertEquals(1, pkg.stopCount)
            assertEquals(listOf("Faro", "Canción de cuna"), pkg.songs.map { it.title })
            val first = pkg.items.first() as SetlistItem.SongItem
            assertEquals(pkg.songs.first().id, first.songId)
            assertEquals("G", first.keyOverride)
            assertEquals("End on E", first.transitionText)
            assertEquals(clock.instant, pkg.exportedAt)
        }

    @Test
    fun exportAMissingSetlistIsNull() =
        runTest {
            assertNull(transfer.exportPackage(sala.id))
        }

    @Test
    fun importIntoABandCreatesANewSetlistWithTheSameContent() =
        runTest {
            prepareSender()
            val pkg = transfer.exportPackage(sala.id)!!

            val id = transfer.import(pkg, sabados.id)

            val imported = setlists.observe(id).first()!!
            assertEquals(sabados.id, imported.bandId)
            assertEquals("Concierto Sala Norte", imported.name)
            assertEquals(LocalDate(2026, 11, 20), imported.date)
            assertEquals(25, imported.pauseSeconds)
            val items = setlists.observeItems(id).first()
            val original = setlists.observeItems(sala.id).first()
            // Same content apart from the item ids; the songs already existed, so the same songs are used.
            assertEquals(original.map { it.withoutId() }, items.map { it.withoutId() })
            assertTrue(items.none { item -> original.any { it.id == item.id } })
        }

    @Test
    fun existingSetlistWithTheSameNameIsLeftUnchanged() =
        runTest {
            prepareSender()
            val before = setlists.observeItems(sala.id).first()

            val id = transfer.import(transfer.exportPackage(sala.id)!!, mareas.id)

            assertNotEquals(sala.id, id)
            assertEquals(2, setlists.observeSetlists(mareas.id).first().count { it.name == sala.name })
            assertEquals(sala, setlists.observe(sala.id).first())
            assertEquals(before, setlists.observeItems(sala.id).first())
        }

    @Test
    fun songAlreadyInTheCatalogueIsReusedIgnoringCaseAndAccents() =
        runTest {
            prepareSender()
            val pkg = transfer.exportPackage(sala.id)!!
            val renamed =
                pkg.copy(
                    songs =
                        pkg.songs.map {
                            if (it.title ==
                                "Canción de cuna"
                            ) {
                                it.copy(title = "CANCION DE CUNA")
                            } else {
                                it
                            }
                        },
                )

            transfer.import(renamed, sabados.id)

            assertEquals(2, songs.observeSongs().first().size)
        }

    @Test
    fun newSongIsAddedToTheCatalogueAndToTheBandsRepertoire() =
        runTest {
            prepareSender()
            val pkg = transfer.exportPackage(sala.id)!!
            val withNewArtist =
                pkg.copy(songs = pkg.songs.map { if (it.title == "Faro") it.copy(artist = "Otra banda") else it })

            val id = transfer.import(withNewArtist, sabados.id)

            val catalogue = songs.observeSongs().first()
            assertEquals(3, catalogue.size)
            val newFaro = catalogue.single { it.artist == "Otra banda" }
            assertEquals(setOf(sabados.id), songs.observeBandsOf(newFaro.id).first())
            // A reused song is not changed: its repertoire stays as it was.
            assertEquals(emptySet(), songs.observeBandsOf(cuna.id).first())
            val items = setlists.observeItems(id).first().filterIsInstance<SetlistItem.SongItem>()
            assertEquals(newFaro.id, items.first().songId)
            assertEquals(newFaro.id, items.last().songId, "both occurrences use the same new song")
        }

    private fun SetlistItem.withoutId(): SetlistItem =
        when (this) {
            is SetlistItem.SongItem -> copy(id = SetlistItemId(""))
            is SetlistItem.Stop -> copy(id = SetlistItemId(""))
        }
}
