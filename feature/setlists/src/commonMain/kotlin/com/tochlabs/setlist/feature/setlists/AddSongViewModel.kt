package com.tochlabs.setlist.feature.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SongResult(
    val song: Song,
    /** Marked as already present, but it can still be added. */
    val alreadyInSetlist: Boolean,
)

/** Confirmation after adding; [repeated] warns that the song was already in the setlist. */
data class AddSongMessage(
    val title: String,
    val repeated: Boolean,
)

data class AddSongUiState(
    val query: String = "",
    val results: List<SongResult> = emptyList(),
    val message: AddSongMessage? = null,
) {
    /** Nothing matches the typed text: offer to create a song with it as the title. */
    val offerCreate: Boolean get() = query.isNotBlank() && results.isEmpty()
}

/** Add song sheet: search the own catalogue and append songs (specs/song-catalog, specs/setlists). */
@OptIn(ExperimentalCoroutinesApi::class)
class AddSongViewModel(
    private val setlistId: SetlistId,
    private val setlists: SetlistRepository,
    private val songs: SongRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val message = MutableStateFlow<AddSongMessage?>(null)

    val state: StateFlow<AddSongUiState> =
        combine(
            query,
            query.flatMapLatest { songs.search(it) },
            setlists.observeItems(setlistId),
            message,
        ) { query, matches, items, message ->
            val present = items.songIds()
            AddSongUiState(
                query = query,
                results = matches.map { SongResult(it, it.id in present) },
                message = message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AddSongUiState())

    fun onQueryChange(text: String) {
        query.value = text
    }

    /** Appends [song] as the last item, warning when it was already there. */
    fun add(song: Song) {
        viewModelScope.launch { append(song) }
    }

    /** Creates a song titled with the search text and appends it. */
    fun createAndAdd() {
        val title = validateRequiredText(query.value)
        if (title !is Validated.Valid) return
        viewModelScope.launch {
            val song = Song(SongId(ids.newId()), title.value)
            songs.save(song)
            append(song)
            query.value = ""
        }
    }

    private suspend fun append(song: Song) {
        val repeated = song.id in setlists.observeItems(setlistId).first().songIds()
        setlists.addItem(setlistId, SetlistItem.SongItem(SetlistItemId(ids.newId()), song.id))
        message.value = AddSongMessage(song.title, repeated)
    }

    fun messageShown() {
        message.value = null
    }
}

private fun List<SetlistItem>.songIds(): Set<SongId> =
    filterIsInstance<SetlistItem.SongItem>()
        .map {
            it.songId
        }.toSet()
