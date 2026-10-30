package com.tochlabs.setlist.feature.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.domain.validation.SHORT_TEXT_MAX_LENGTH
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_delete
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.core.ui.resources.error_required
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.songs.resources.Res
import com.tochlabs.setlist.feature.songs.resources.action_ok
import com.tochlabs.setlist.feature.songs.resources.song_artist
import com.tochlabs.setlist.feature.songs.resources.song_bands
import com.tochlabs.setlist.feature.songs.resources.song_bands_none
import com.tochlabs.setlist.feature.songs.resources.song_bpm
import com.tochlabs.setlist.feature.songs.resources.song_delete
import com.tochlabs.setlist.feature.songs.resources.song_delete_body
import com.tochlabs.setlist.feature.songs.resources.song_delete_title
import com.tochlabs.setlist.feature.songs.resources.song_duration
import com.tochlabs.setlist.feature.songs.resources.song_duration_invalid
import com.tochlabs.setlist.feature.songs.resources.song_edit
import com.tochlabs.setlist.feature.songs.resources.song_in_use_body
import com.tochlabs.setlist.feature.songs.resources.song_in_use_title
import com.tochlabs.setlist.feature.songs.resources.song_key
import com.tochlabs.setlist.feature.songs.resources.song_minutes
import com.tochlabs.setlist.feature.songs.resources.song_missing_duration
import com.tochlabs.setlist.feature.songs.resources.song_new
import com.tochlabs.setlist.feature.songs.resources.song_seconds
import com.tochlabs.setlist.feature.songs.resources.song_title
import com.tochlabs.setlist.feature.songs.resources.song_tuning
import com.tochlabs.setlist.feature.songs.resources.song_tuning_hint
import com.tochlabs.setlist.feature.songs.resources.song_tuning_standard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Common alternative tunings offered as one-tap suggestions; any text is allowed. */
private val tuningSuggestions = listOf("Drop D", "Eb", "D", "Drop C", "DADGAD", "Open G")

/** Create a song, or edit and delete [songId]. Calls [onDone] after saving, deleting or closing. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongFormScreen(
    songId: SongId?,
    onDone: () -> Unit,
    viewModel: SongFormViewModel = koinViewModel(key = "song-form-${songId?.value}") { parametersOf(songId?.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved, state.deleted) { if (state.saved || state.deleted) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) Res.string.song_new else Res.string.song_edit)) },
                navigationIcon = {
                    ActionIcon(
                        CoreRes.drawable.ic_close,
                        stringResource(CoreRes.string.action_cancel),
                        onDone,
                    )
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                    ) { Text(stringResource(CoreRes.string.action_save)) }
                },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MainFields(state, viewModel)
            DurationFields(state, viewModel)
            TuningField(state.tuning, viewModel::onTuningChange)
            BandsSection(state, viewModel::toggleBand)
            if (!state.isNew) {
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(Res.string.song_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = state.title,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
    if (state.blockingSetlists.isNotEmpty()) {
        InUseDialog(state.blockingSetlists, onDismiss = viewModel::dismissBlocking)
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.song_delete_title, title)) },
        text = { Text(stringResource(Res.string.song_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(CoreRes.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreRes.string.action_cancel)) } },
    )
}

/** Deletion refused: lists the setlists that use the song (specs/song-catalog). */
@Composable
private fun InUseDialog(
    setlists: List<String>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.song_in_use_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.song_in_use_body))
                setlists.forEach { Text("• $it") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_ok)) } },
    )
}

private val numbers = KeyboardOptions(keyboardType = KeyboardType.Number)

@Composable
private fun MainFields(
    state: SongFormUiState,
    viewModel: SongFormViewModel,
) {
    val wide = Modifier.fillMaxWidth()
    val sentences = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    OutlinedTextField(
        value = state.title,
        onValueChange = viewModel::onTitleChange,
        label = { Text(stringResource(Res.string.song_title)) },
        isError = state.titleError,
        supportingText = if (state.titleError) ({ Text(stringResource(CoreRes.string.error_required)) }) else null,
        singleLine = true,
        keyboardOptions = sentences,
        modifier = wide,
    )
    OutlinedTextField(
        value = state.artist,
        onValueChange = viewModel::onArtistChange,
        label = { Text(stringResource(Res.string.song_artist)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = wide,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.key,
            onValueChange = viewModel::onKeyChange,
            label = { Text(stringResource(Res.string.song_key)) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = state.bpm,
            onValueChange = viewModel::onBpmChange,
            label = { Text(stringResource(Res.string.song_bpm)) },
            singleLine = true,
            keyboardOptions = numbers,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Minutes and seconds, with the "totals will be incomplete" note when empty. */
@Composable
private fun DurationFields(
    state: SongFormUiState,
    viewModel: SongFormViewModel,
) {
    Text(stringResource(Res.string.song_duration), style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.minutes,
            onValueChange = viewModel::onMinutesChange,
            label = { Text(stringResource(Res.string.song_minutes)) },
            isError = state.durationError,
            singleLine = true,
            keyboardOptions = numbers,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = state.seconds,
            onValueChange = viewModel::onSecondsChange,
            label = { Text(stringResource(Res.string.song_seconds)) },
            isError = state.durationError,
            singleLine = true,
            keyboardOptions = numbers,
            modifier = Modifier.weight(1f),
        )
    }
    when {
        state.durationError ->
            Text(
                stringResource(Res.string.song_duration_invalid),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        state.missingDuration ->
            Text(
                stringResource(Res.string.song_missing_duration),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
    }
}

/** Free text up to 20 characters with a counter and one-tap suggestions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TuningField(
    tuning: String,
    onTuningChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = tuning,
        onValueChange = onTuningChange,
        label = { Text(stringResource(Res.string.song_tuning)) },
        placeholder = { Text(stringResource(Res.string.song_tuning_standard)) },
        supportingText = {
            Row {
                Text(stringResource(Res.string.song_tuning_hint), modifier = Modifier.weight(1f))
                Text("${tuning.length} / $SHORT_TEXT_MAX_LENGTH")
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SuggestionChip(
            onClick = { onTuningChange("") },
            label = { Text(stringResource(Res.string.song_tuning_standard)) },
        )
        tuningSuggestions.forEach { suggestion ->
            SuggestionChip(onClick = { onTuningChange(suggestion) }, label = { Text(suggestion) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BandsSection(
    state: SongFormUiState,
    onToggle: (BandId) -> Unit,
) {
    Text(stringResource(Res.string.song_bands), style = MaterialTheme.typography.titleSmall)
    if (state.bands.isEmpty()) {
        Text(
            stringResource(Res.string.song_bands_none),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.bands.forEach { band ->
            FilterChip(
                selected = band.id in state.selectedBands,
                onClick = { onToggle(band.id) },
                label = { Text(band.name) },
                leadingIcon = { BandDot(band.color) },
            )
        }
    }
}
