package com.tochlabs.setlist.feature.bands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BandRow(
    val band: Band,
    val songCount: Int,
    val setlistCount: Int,
)

sealed interface BandsMessage {
    data class Archived(
        val bandName: String,
    ) : BandsMessage
}

data class BandsUiState(
    val loading: Boolean = true,
    val bands: List<BandRow> = emptyList(),
    val hasHistory: Boolean = false,
    val message: BandsMessage? = null,
)

/** Active band list. The only removal it offers is archiving (specs/bands, "No direct deletion"). */
class BandsViewModel(
    private val bands: BandRepository,
    private val songs: SongRepository,
    private val setlists: SetlistRepository,
) : ViewModel() {
    private val message = MutableStateFlow<BandsMessage?>(null)

    val state: StateFlow<BandsUiState> =
        combine(rows(), bands.observeArchived(), message) { rows, archived, message ->
            BandsUiState(loading = false, bands = rows, hasHistory = archived.isNotEmpty(), message = message)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), BandsUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rows(): Flow<List<BandRow>> =
        bands.observeActive().flatMapLatest { active ->
            if (active.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(active.map(::rowOf)) { it.toList() }
            }
        }

    private fun rowOf(band: Band): Flow<BandRow> =
        combine(songs.observeSongs(band.id), setlists.observeSetlists(band.id)) { songs, setlists ->
            BandRow(band, songCount = songs.size, setlistCount = setlists.size)
        }

    fun archive(id: BandId) {
        viewModelScope.launch {
            val band = bands.observe(id).first() ?: return@launch
            bands.archive(id)
            message.value = BandsMessage.Archived(band.name)
        }
    }

    fun messageShown() {
        message.value = null
    }
}

internal const val STOP_TIMEOUT_MILLIS = 5_000L
