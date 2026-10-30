package com.tochlabs.setlist.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.DeleteSongResult
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.validation.SHORT_TEXT_MAX_LENGTH
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.limitShortText
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Form fields are kept as typed text; they are converted when saving. */
data class SongFormUiState(
    val isNew: Boolean,
    val loading: Boolean = true,
    val title: String = "",
    val artist: String = "",
    val key: String = "",
    val bpm: String = "",
    val tuning: String = "",
    val minutes: String = "",
    val seconds: String = "",
    /** Active bands the user can mark. */
    val bands: List<Band> = emptyList(),
    val selectedBands: Set<BandId> = emptySet(),
    val titleError: Boolean = false,
    val durationError: Boolean = false,
    /** Names of the setlists that stop a deletion; empty when there is nothing to show. */
    val blockingSetlists: List<String> = emptyList(),
    val saved: Boolean = false,
    val deleted: Boolean = false,
) {
    /** No duration typed: the form warns that setlist totals will be incomplete. */
    val missingDuration: Boolean get() = minutes.isBlank() && seconds.isBlank()
}

/** Create a song, or edit and delete one when [songId] is given (specs/song-catalog). */
class SongFormViewModel(
    private val songId: SongId?,
    private val songs: SongRepository,
    private val bands: BandRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SongFormUiState(isNew = songId == null))
    val state: StateFlow<SongFormUiState> = mutableState.asStateFlow()

    /** Repertoire entries loaded for an existing song, kept to preserve those of archived bands. */
    private var loadedBands: Set<BandId> = emptySet()

    init {
        viewModelScope.launch {
            val active = bands.observeActive().first()
            val song = songId?.let { songs.observe(it).first() }
            loadedBands = songId?.let { songs.observeBandsOf(it).first() }.orEmpty()
            mutableState.update { state ->
                val base = state.copy(loading = false, bands = active)
                if (song == null) base else base.withSong(song, loadedBands)
            }
        }
    }

    private fun SongFormUiState.withSong(
        song: Song,
        bandIds: Set<BandId>,
    ): SongFormUiState {
        val duration = song.durationSeconds
        return copy(
            title = song.title,
            artist = song.artist.orEmpty(),
            key = song.originalKey.orEmpty(),
            bpm = song.originalBpm?.toString().orEmpty(),
            tuning = song.originalTuning.orEmpty(),
            minutes = duration?.let { (it / SECONDS_PER_MINUTE).toString() }.orEmpty(),
            seconds = duration?.let { (it % SECONDS_PER_MINUTE).toString() }.orEmpty(),
            selectedBands = bandIds,
        )
    }

    fun onTitleChange(text: String) = mutableState.update { it.copy(title = text, titleError = false) }

    fun onArtistChange(text: String) = mutableState.update { it.copy(artist = text) }

    fun onKeyChange(text: String) = mutableState.update { it.copy(key = text) }

    fun onBpmChange(text: String) = mutableState.update { it.copy(bpm = digits(text, MAX_BPM_DIGITS)) }

    /** Free text, at most 20 characters: extra characters are dropped as they are typed. */
    fun onTuningChange(text: String) = mutableState.update { it.copy(tuning = text.take(SHORT_TEXT_MAX_LENGTH)) }

    fun onMinutesChange(text: String) =
        mutableState.update { it.copy(minutes = digits(text, MAX_MINUTE_DIGITS), durationError = false) }

    fun onSecondsChange(text: String) =
        mutableState.update { it.copy(seconds = digits(text, MAX_SECOND_DIGITS), durationError = false) }

    fun toggleBand(id: BandId) =
        mutableState.update {
            it.copy(selectedBands = if (id in it.selectedBands) it.selectedBands - id else it.selectedBands + id)
        }

    fun save() {
        val form = mutableState.value
        if (form.loading || form.saved) return
        val title = validateRequiredText(form.title)
        val duration = parseDuration(form.minutes, form.seconds)
        if (title is Validated.Invalid || duration is DurationInput.Invalid) {
            mutableState.update {
                it.copy(titleError = title is Validated.Invalid, durationError = duration is DurationInput.Invalid)
            }
            return
        }
        val song =
            Song(
                id = songId ?: SongId(ids.newId()),
                title = (title as Validated.Valid).value,
                artist = form.artist.trim().ifEmpty { null },
                originalKey = form.key.trim().ifEmpty { null },
                originalBpm = form.bpm.toIntOrNull()?.takeIf { it > 0 },
                originalTuning = limitShortText(form.tuning),
                durationSeconds = (duration as DurationInput.Valid).seconds,
            )
        // The form only shows active bands; entries of archived bands are kept as they were.
        val activeIds = form.bands.map { it.id }.toSet()
        val bandIds = form.selectedBands + (loadedBands - activeIds)
        viewModelScope.launch {
            songs.save(song)
            songs.setBands(song.id, bandIds)
            mutableState.update { it.copy(saved = true) }
        }
    }

    /** Deletes the song, or lists the setlists that use it (specs/song-catalog). */
    fun delete() {
        val id = songId ?: return
        viewModelScope.launch {
            when (val result = songs.delete(id)) {
                DeleteSongResult.Deleted -> mutableState.update { it.copy(deleted = true) }
                is DeleteSongResult.InUse ->
                    mutableState.update { state -> state.copy(blockingSetlists = result.setlists.map { it.name }) }
            }
        }
    }

    fun dismissBlocking() = mutableState.update { it.copy(blockingSetlists = emptyList()) }
}

private sealed interface DurationInput {
    data class Valid(
        val seconds: Int?,
    ) : DurationInput

    data object Invalid : DurationInput
}

/** Both fields empty, or zero in total, mean no duration. Seconds must be below 60. */
private fun parseDuration(
    minutes: String,
    seconds: String,
): DurationInput {
    val m = minutes.toIntOrNull() ?: 0
    val s = seconds.toIntOrNull() ?: 0
    if (s >= SECONDS_PER_MINUTE) return DurationInput.Invalid
    val total = m * SECONDS_PER_MINUTE + s
    return DurationInput.Valid(total.takeIf { it > 0 })
}

private fun digits(
    text: String,
    max: Int,
): String = text.filter { it.isDigit() }.take(max)

private const val SECONDS_PER_MINUTE = 60
private const val MAX_BPM_DIGITS = 3
private const val MAX_MINUTE_DIGITS = 3
private const val MAX_SECOND_DIGITS = 2
