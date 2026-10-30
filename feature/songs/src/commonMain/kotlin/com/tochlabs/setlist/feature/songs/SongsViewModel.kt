package com.tochlabs.setlist.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SongRow(
    val song: Song,
    /** Active bands that play the song, in band list order. */
    val bands: List<Band>,
) {
    /** Listed with a "no duration" label (specs/song-catalog, "Optional duration"). */
    val missingDuration: Boolean get() = song.durationSeconds == null
}

data class SongsUiState(
    val loading: Boolean = true,
    /** Active bands, offered as filter chips. */
    val bands: List<Band> = emptyList(),
    val selectedBand: BandId? = null,
    val query: String = "",
    val songs: List<SongRow> = emptyList(),
    val catalogueEmpty: Boolean = true,
)

/** Song catalogue with band filter and search by title and artist. */
@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(
    private val songs: SongRepository,
    bands: BandRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedBand = MutableStateFlow<BandId?>(null)
    private val activeBands = bands.observeActive()

    /** For each song, the active bands that play it. */
    private val membership: Flow<Map<SongId, List<Band>>> =
        activeBands.flatMapLatest { active ->
            if (active.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    active.map { band ->
                        songs.observeSongs(band.id).map { list -> band to list }
                    },
                ) { repertoires ->
                    buildMap<SongId, MutableList<Band>> {
                        repertoires.forEach { (band, list) ->
                            list.forEach { getOrPut(it.id) { mutableListOf() } += band }
                        }
                    }
                }
            }
        }

    // An empty query returns the whole catalogue.
    private val matches: Flow<List<Song>> = query.flatMapLatest { songs.search(it) }

    private val filters = combine(query, selectedBand) { query, band -> query to band }

    val state: StateFlow<SongsUiState> =
        combine(
            activeBands,
            membership,
            matches,
            songs.observeSongs(),
            filters,
        ) { active, bandsOf, matches, all, filters ->
            val (query, selected) = filters
            // A band archived while selected is no longer a valid filter.
            val band = selected?.takeIf { id -> active.any { it.id == id } }
            SongsUiState(
                loading = false,
                bands = active,
                selectedBand = band,
                query = query,
                songs =
                    matches
                        .map { SongRow(it, bandsOf[it.id].orEmpty()) }
                        .filter { row -> band == null || row.bands.any { it.id == band } },
                catalogueEmpty = all.isEmpty(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SongsUiState())

    fun selectBand(id: BandId?) {
        selectedBand.value = id
    }

    fun onQueryChange(text: String) {
        query.value = text
    }
}

internal const val STOP_TIMEOUT_MILLIS = 5_000L
