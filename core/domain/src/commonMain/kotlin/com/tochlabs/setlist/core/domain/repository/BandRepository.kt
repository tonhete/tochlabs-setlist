package com.tochlabs.setlist.core.domain.repository

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.flow.Flow

interface BandRepository {
    /** Bands not archived, by name. */
    fun observeActive(): Flow<List<Band>>

    /** Archived bands (the history), most recently archived first. */
    fun observeArchived(): Flow<List<Band>>

    fun observe(id: BandId): Flow<Band?>

    /** Inserts or updates. */
    suspend fun save(band: Band)

    /** Moves the band, with its setlists and events, to the history. */
    suspend fun archive(id: BandId)

    suspend fun restore(id: BandId)

    /** What a permanent deletion would remove, shown before confirming. */
    suspend fun deletionSummary(id: BandId): BandDeletionSummary

    /** Permanent deletion; only allowed for archived bands. Songs stay in the catalogue. */
    suspend fun deleteArchived(id: BandId): DeleteBandResult
}

data class BandDeletionSummary(
    val setlists: Int,
    val events: Int,
    val repertoireEntries: Int,
)

enum class DeleteBandResult { DELETED, NOT_ARCHIVED }
