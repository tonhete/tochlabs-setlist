package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.domain.repository.DeleteSongResult
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Scenarios from specs/song-catalog.
internal class SongRepositoryTest : RepositoryTestBase() {
    @Test
    fun createASongWithAllData() =
        runTest {
            val full = Song(SongId("s1"), "Faro", "Mareas", "F#m", 128, "Drop D", 245)
            songs.save(full)
            assertEquals(full, songs.observe(full.id).first())
        }

    @Test
    fun createASongWithTitleOnly() =
        runTest {
            songs.save(song("Faro"))
            assertEquals(Song(SongId("Faro"), "Faro"), songs.observe(SongId("Faro")).first())
        }

    @Test
    fun editingASongKeepsOneRow() =
        runTest {
            songs.save(song("Faro"))
            songs.save(song("Faro").copy(durationSeconds = 200))
            assertEquals(listOf(200), songs.observeSongs().first().map { it.durationSeconds })
        }

    @Test
    fun accentInsensitiveSearch() =
        runTest {
            songs.save(song("Canción de cuna"))
            songs.save(song("Faro"))
            assertEquals(listOf("Canción de cuna"), songs.search("cancion").first().map { it.title })
        }

    @Test
    fun searchMatchesTheArtist() =
        runTest {
            songs.save(song("Faro", artist = "Ana Pérez"))
            assertEquals(listOf("Faro"), songs.search("PEREZ").first().map { it.title })
        }

    @Test
    fun searchTreatsWildcardsAsText() =
        runTest {
            songs.save(song("100% amor"))
            songs.save(song("Faro"))
            assertEquals(listOf("100% amor"), songs.search("%").first().map { it.title })
        }

    @Test
    fun songSharedByTwoBands() =
        runTest {
            val mareas = band("Mareas")
            val sabados = band("Los Sábados")
            bands.save(mareas)
            bands.save(sabados)
            songs.save(song("Faro"))

            songs.setBands(song("Faro").id, setOf(mareas.id, sabados.id))

            assertEquals(setOf(mareas.id, sabados.id), songs.observeBandsOf(song("Faro").id).first())
            assertEquals(listOf("Faro"), songs.observeSongs(sabados.id).first().map { it.title })
        }

    @Test
    fun filterSongsByBand() =
        runTest {
            val mareas = band("Mareas")
            bands.save(mareas)
            songs.save(song("Faro"))
            songs.save(song("Otra"))
            songs.setBands(song("Faro").id, setOf(mareas.id))

            assertEquals(listOf("Faro"), songs.observeSongs(mareas.id).first().map { it.title })
        }

    @Test
    fun deleteAnUnusedSong() =
        runTest {
            val mareas = band("Mareas")
            bands.save(mareas)
            songs.save(song("Faro"))
            songs.setBands(song("Faro").id, setOf(mareas.id))

            assertEquals(DeleteSongResult.Deleted, songs.delete(song("Faro").id))

            assertTrue(songs.observeSongs().first().isEmpty())
            assertTrue(songs.observeSongs(mareas.id).first().isEmpty())
        }

    @Test
    fun deleteASongUsedInSetlists() =
        runTest {
            val mareas = band("Mareas")
            bands.save(mareas)
            songs.save(song("Faro"))
            val salaNorte = setlist("Sala Norte", mareas)
            setlists.save(salaNorte)
            setlists.addItem(salaNorte.id, songItem("i1", song("Faro")))

            val result = songs.delete(song("Faro").id)

            assertEquals(DeleteSongResult.InUse(listOf(salaNorte)), result)
            assertEquals(1, songs.observeSongs().first().size)
        }
}
