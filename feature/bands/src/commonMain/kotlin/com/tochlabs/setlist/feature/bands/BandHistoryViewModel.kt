package com.tochlabs.setlist.feature.bands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.BandDeletionSummary
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.DeleteBandResult
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A deletion waiting for the user's confirmation, with what it would remove. */
data class PendingDeletion(
    val band: Band,
    val summary: BandDeletionSummary,
)

sealed interface HistoryMessage {
    data class Restored(
        val bandName: String,
    ) : HistoryMessage

    data class Deleted(
        val bandName: String,
    ) : HistoryMessage
}

data class BandHistoryUiState(
    val loading: Boolean = true,
    val bands: List<Band> = emptyList(),
    val pendingDeletion: PendingDeletion? = null,
    val message: HistoryMessage? = null,
)

/** Archived bands: restore, or delete permanently after confirming (specs/bands). */
class BandHistoryViewModel(
    private val bands: BandRepository,
) : ViewModel() {
    private val pendingDeletion = MutableStateFlow<PendingDeletion?>(null)
    private val message = MutableStateFlow<HistoryMessage?>(null)

    val state: StateFlow<BandHistoryUiState> =
        combine(bands.observeArchived(), pendingDeletion, message) { archived, pending, message ->
            BandHistoryUiState(loading = false, bands = archived, pendingDeletion = pending, message = message)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), BandHistoryUiState())

    fun restore(id: BandId) {
        viewModelScope.launch {
            val band = bands.observe(id).first() ?: return@launch
            bands.restore(id)
            message.value = HistoryMessage.Restored(band.name)
        }
    }

    /** First step of a deletion: load the counts to show in the confirmation. */
    fun requestDelete(id: BandId) {
        viewModelScope.launch {
            val band = bands.observe(id).first() ?: return@launch
            pendingDeletion.value = PendingDeletion(band, bands.deletionSummary(id))
        }
    }

    fun cancelDelete() {
        pendingDeletion.value = null
    }

    fun confirmDelete() {
        val pending = pendingDeletion.value ?: return
        pendingDeletion.value = null
        viewModelScope.launch {
            if (bands.deleteArchived(pending.band.id) == DeleteBandResult.DELETED) {
                message.value = HistoryMessage.Deleted(pending.band.name)
            }
        }
    }

    fun messageShown() {
        message.value = null
    }
}
