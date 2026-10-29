package com.tochlabs.setlist.core.data.repository

import androidx.room3.withReadTransaction
import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.Timestamps
import com.tochlabs.setlist.core.data.db.toEntity
import com.tochlabs.setlist.core.data.db.toModel
import com.tochlabs.setlist.core.domain.repository.BandDeletionSummary
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.DeleteBandResult
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomBandRepository(
    private val db: SetlistDatabase,
    private val clock: Clock,
) : BandRepository {
    private val dao = db.bandDao()

    override fun observeActive(): Flow<List<Band>> = dao.observeActive().map { rows -> rows.map { it.toModel() } }

    override fun observeArchived(): Flow<List<Band>> = dao.observeArchived().map { rows -> rows.map { it.toModel() } }

    override fun observe(id: BandId): Flow<Band?> = dao.observe(id.value).map { it?.toModel() }

    /** Saving never changes the archive state; [archive] and [restore] do. */
    override suspend fun save(band: Band) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val existing = dao.get(band.id.value)
            val time = Timestamps(createdAt = existing?.createdAt ?: now, updatedAt = now)
            val row = band.toEntity(archivedAt = existing?.archivedAt, time = time)
            if (existing == null) dao.insert(row) else dao.update(row)
        }
    }

    // Setlists and events have no archive flag: they follow their band (data-model.md).
    override suspend fun archive(id: BandId) {
        val now = clock.nowMillis()
        dao.setArchivedAt(id.value, archivedAt = now, updatedAt = now)
    }

    override suspend fun restore(id: BandId) {
        dao.setArchivedAt(id.value, archivedAt = null, updatedAt = clock.nowMillis())
    }

    override suspend fun deletionSummary(id: BandId): BandDeletionSummary =
        db.withReadTransaction {
            BandDeletionSummary(
                setlists = dao.countSetlists(id.value),
                events = dao.countEvents(id.value),
                repertoireEntries = dao.countRepertoire(id.value),
            )
        }

    /** Foreign keys cascade to setlists, items, events, milestones and repertoire; songs stay. */
    override suspend fun deleteArchived(id: BandId): DeleteBandResult =
        db.withWriteTransaction {
            val band = dao.get(id.value)
            if (band != null && band.archivedAt == null) {
                DeleteBandResult.NOT_ARCHIVED
            } else {
                dao.delete(id.value)
                DeleteBandResult.DELETED
            }
        }
}

internal fun Clock.nowMillis(): Long = now().toEpochMilliseconds()
