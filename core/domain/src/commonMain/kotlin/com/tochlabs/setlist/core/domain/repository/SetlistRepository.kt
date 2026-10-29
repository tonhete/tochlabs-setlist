package com.tochlabs.setlist.core.domain.repository

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import kotlinx.coroutines.flow.Flow

interface SetlistRepository {
    /** Setlists of active bands; with a band, only that band's. */
    fun observeSetlists(bandId: BandId? = null): Flow<List<Setlist>>

    fun observe(id: SetlistId): Flow<Setlist?>

    /** Items in order. */
    fun observeItems(setlistId: SetlistId): Flow<List<SetlistItem>>

    /** Inserts or updates the setlist header (name, date, pause). */
    suspend fun save(setlist: Setlist)

    /** Deletes the setlist and its items; a linked event stays without setlist. */
    suspend fun delete(id: SetlistId)

    /** Appends the item as the last one. */
    suspend fun addItem(
        setlistId: SetlistId,
        item: SetlistItem,
    )

    suspend fun updateItem(item: SetlistItem)

    suspend fun removeItem(id: SetlistItemId)

    /** Rewrites the order of the setlist in one transaction. */
    suspend fun reorder(
        setlistId: SetlistId,
        orderedIds: List<SetlistItemId>,
    )
}
