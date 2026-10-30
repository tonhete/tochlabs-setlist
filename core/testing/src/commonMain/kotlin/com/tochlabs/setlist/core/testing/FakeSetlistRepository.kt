package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSetlistRepository(
    private val store: FakeStore,
) : SetlistRepository {
    override fun observeSetlists(bandId: BandId?): Flow<List<Setlist>> =
        combine(store.setlists, store.bands) { setlists, bands ->
            val active = bands.filterNot { it.isArchived }.map { it.id }.toSet()
            setlists.filter { it.bandId in active && (bandId == null || it.bandId == bandId) }
        }

    override fun observe(id: SetlistId): Flow<Setlist?> = store.setlists.map { list -> list.find { it.id == id } }

    override fun observeItems(setlistId: SetlistId): Flow<List<SetlistItem>> =
        store.items.map { it[setlistId].orEmpty() }

    override suspend fun save(setlist: Setlist) = store.setlists.upsert(setlist) { it.id == setlist.id }

    override suspend fun delete(id: SetlistId) {
        store.setlists.update { list -> list.filterNot { it.id == id } }
        store.items.update { it - id }
        store.events.update { list -> list.map { if (it.setlistId == id) it.copy(setlistId = null) else it } }
    }

    override suspend fun addItem(
        setlistId: SetlistId,
        item: SetlistItem,
    ) = store.items.update { it + (setlistId to it[setlistId].orEmpty() + item) }

    override suspend fun updateItem(item: SetlistItem) =
        store.items.update { map ->
            map.mapValues { (_, items) -> items.map { if (it.id == item.id) item else it } }
        }

    override suspend fun removeItem(id: SetlistItemId) =
        store.items.update { map -> map.mapValues { (_, items) -> items.filterNot { it.id == id } } }

    override suspend fun reorder(
        setlistId: SetlistId,
        orderedIds: List<SetlistItemId>,
    ) = store.items.update { map ->
        val byId = map[setlistId].orEmpty().associateBy { it.id }
        map + (setlistId to orderedIds.mapNotNull { byId[it] })
    }
}
