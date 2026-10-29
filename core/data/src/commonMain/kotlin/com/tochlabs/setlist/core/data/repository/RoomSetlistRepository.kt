package com.tochlabs.setlist.core.data.repository

import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.Timestamps
import com.tochlabs.setlist.core.data.db.toEntity
import com.tochlabs.setlist.core.data.db.toModel
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomSetlistRepository(
    private val db: SetlistDatabase,
    private val clock: Clock,
) : SetlistRepository {
    private val dao = db.setlistDao()
    private val items = db.setlistItemDao()

    override fun observeSetlists(bandId: BandId?): Flow<List<Setlist>> {
        val rows = if (bandId == null) dao.observeActive() else dao.observeActiveByBand(bandId.value)
        return rows.map { list -> list.map { it.toModel() } }
    }

    override fun observe(id: SetlistId): Flow<Setlist?> = dao.observe(id.value).map { it?.toModel() }

    override fun observeItems(setlistId: SetlistId): Flow<List<SetlistItem>> =
        items.observeBySetlist(setlistId.value).map { rows -> rows.map { it.toModel() } }

    override suspend fun save(setlist: Setlist) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val existing = dao.get(setlist.id.value)
            val row = setlist.toEntity(Timestamps(existing?.createdAt ?: now, now))
            if (existing == null) dao.insert(row) else dao.update(row)
        }
    }

    /** Items cascade; a linked event keeps existing with no setlist (SET NULL). */
    override suspend fun delete(id: SetlistId) {
        dao.delete(id.value)
    }

    override suspend fun addItem(
        setlistId: SetlistId,
        item: SetlistItem,
    ) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val position = items.nextPosition(setlistId.value)
            items.insert(item.toEntity(setlistId.value, position, Timestamps(now, now)))
        }
    }

    /** Keeps the item in its setlist and position. */
    override suspend fun updateItem(item: SetlistItem) {
        db.withWriteTransaction {
            val existing = items.get(item.id.value) ?: return@withWriteTransaction
            val time = Timestamps(existing.createdAt, clock.nowMillis())
            items.update(item.toEntity(existing.setlistId, existing.position, time))
        }
    }

    override suspend fun removeItem(id: SetlistItemId) {
        db.withWriteTransaction {
            val existing = items.get(id.value) ?: return@withWriteTransaction
            items.delete(id.value)
            renumber(items.getBySetlist(existing.setlistId).map { it.id })
        }
    }

    /** [orderedIds] must contain exactly the items of the setlist. */
    override suspend fun reorder(
        setlistId: SetlistId,
        orderedIds: List<SetlistItemId>,
    ) {
        db.withWriteTransaction {
            val current = items.getBySetlist(setlistId.value).map { it.id }
            val requested = orderedIds.map { it.value }
            require(current.sorted() == requested.sorted()) { "Reorder must list every item of the setlist once" }
            renumber(requested)
        }
    }

    // Plain (non-unique) position index: rewriting positions inside the transaction is safe.
    private suspend fun renumber(ids: List<String>) {
        ids.forEachIndexed { position, id -> items.setPosition(id, position) }
    }
}
