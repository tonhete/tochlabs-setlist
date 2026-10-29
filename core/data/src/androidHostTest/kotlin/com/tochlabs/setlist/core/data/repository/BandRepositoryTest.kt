package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.domain.repository.BandDeletionSummary
import com.tochlabs.setlist.core.domain.repository.DeleteBandResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

// Scenarios from specs/bands.
internal class BandRepositoryTest : RepositoryTestBase() {
    private val mareas = band("Mareas")

    private suspend fun mareasWithData() {
        bands.save(mareas)
        songs.save(song("Faro"))
        songs.setBands(song("Faro").id, setOf(mareas.id))
        setlists.save(setlist("Sala Norte", mareas))
        setlists.addItem(setlist("Sala Norte", mareas).id, songItem("i1", song("Faro")))
        events.save(event("Concierto", mareas, setlist("Sala Norte", mareas)))
    }

    @Test
    fun createABand() =
        runTest {
            bands.save(mareas)
            assertEquals(listOf(mareas), bands.observeActive().first())
        }

    @Test
    fun archiveABandWithSetlists() =
        runTest {
            mareasWithData()
            clock.instant = Instant.fromEpochMilliseconds(5_000)

            bands.archive(mareas.id)

            assertTrue(bands.observeActive().first().isEmpty())
            assertTrue(setlists.observeSetlists().first().isEmpty())
            assertTrue(events.observeEvents().first().isEmpty())
            assertEquals(
                Instant.fromEpochMilliseconds(5_000),
                bands
                    .observeArchived()
                    .first()
                    .single()
                    .archivedAt,
            )
        }

    @Test
    fun songsAreNotArchived() =
        runTest {
            mareasWithData()
            bands.archive(mareas.id)
            assertEquals(1, songs.observeSongs().first().size)
        }

    @Test
    fun restoreFromHistory() =
        runTest {
            mareasWithData()
            bands.archive(mareas.id)

            bands.restore(mareas.id)

            assertEquals(listOf(mareas), bands.observeActive().first())
            assertEquals(1, setlists.observeSetlists().first().size)
            assertEquals(1, events.observeEvents().first().size)
            assertEquals(1, setlists.observeItems(setlist("Sala Norte", mareas).id).first().size)
        }

    @Test
    fun savingAnArchivedBandKeepsItArchived() =
        runTest {
            bands.save(mareas)
            bands.archive(mareas.id)
            bands.save(mareas.copy(name = "Mareas 2"))
            assertTrue(bands.observeActive().first().isEmpty())
        }

    @Test
    fun confirmBeforeDeletingShowsWhatWillBeRemoved() =
        runTest {
            mareasWithData()
            assertEquals(
                BandDeletionSummary(setlists = 1, events = 1, repertoireEntries = 1),
                bands.deletionSummary(mareas.id),
            )
        }

    @Test
    fun noDirectDeletionOfAnActiveBand() =
        runTest {
            mareasWithData()
            assertEquals(DeleteBandResult.NOT_ARCHIVED, bands.deleteArchived(mareas.id))
            assertNotNull(bands.observe(mareas.id).first())
        }

    @Test
    fun confirmedDeletion() =
        runTest {
            mareasWithData()
            bands.archive(mareas.id)

            assertEquals(DeleteBandResult.DELETED, bands.deleteArchived(mareas.id))

            assertNull(bands.observe(mareas.id).first())
            assertNull(setlists.observe(setlist("Sala Norte", mareas).id).first())
            assertTrue(bands.observeArchived().first().isEmpty())
            assertEquals(listOf(song("Faro")), songs.observeSongs().first())
            assertTrue(songs.observeBandsOf(song("Faro").id).first().isEmpty())
        }
}
