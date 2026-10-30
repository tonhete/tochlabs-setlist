package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.repository.BandDeletionSummary
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.DeleteBandResult
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

class FakeBandRepository(
    private val store: FakeStore,
    private val clock: Clock = FakeClock(),
) : BandRepository {
    override fun observeActive(): Flow<List<Band>> =
        store.bands.map { list -> list.filterNot { it.isArchived }.sortedBy { it.name.lowercase() } }

    override fun observeArchived(): Flow<List<Band>> =
        store.bands.map { list -> list.filter { it.isArchived }.sortedByDescending { it.archivedAt } }

    override fun observe(id: BandId): Flow<Band?> = store.bands.map { list -> list.find { it.id == id } }

    /** Like the real repository, saving keeps the archive state. */
    override suspend fun save(band: Band) {
        val existing = store.bands.value.find { it.id == band.id }
        store.bands.upsert(band.copy(archivedAt = existing?.archivedAt)) { it.id == band.id }
    }

    override suspend fun archive(id: BandId) = setArchivedAt(id, clock.now())

    override suspend fun restore(id: BandId) = setArchivedAt(id, null)

    private fun setArchivedAt(
        id: BandId,
        at: Instant?,
    ) = store.bands.update { list -> list.map { if (it.id == id) it.copy(archivedAt = at) else it } }

    override suspend fun deletionSummary(id: BandId): BandDeletionSummary =
        BandDeletionSummary(
            setlists = store.setlists.value.count { it.bandId == id },
            events = store.events.value.count { it.bandId == id },
            repertoireEntries = store.repertoire.value.count { it.first == id },
        )

    override suspend fun deleteArchived(id: BandId): DeleteBandResult {
        val band = store.bands.value.find { it.id == id }
        if (band != null && !band.isArchived) return DeleteBandResult.NOT_ARCHIVED
        store.cascadeDeleteBand(id)
        return DeleteBandResult.DELETED
    }
}
