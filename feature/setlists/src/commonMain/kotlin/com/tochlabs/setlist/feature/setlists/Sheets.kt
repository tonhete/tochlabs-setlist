package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tochlabs.setlist.core.domain.validation.SHORT_TEXT_MAX_LENGTH
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.SetlistSheet
import com.tochlabs.setlist.core.ui.component.Tag
import com.tochlabs.setlist.core.ui.format.formatClock
import com.tochlabs.setlist.core.ui.resources.action_add
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_remove
import com.tochlabs.setlist.core.ui.resources.ic_search
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.action_add_song
import com.tochlabs.setlist.feature.setlists.resources.action_add_stop
import com.tochlabs.setlist.feature.setlists.resources.action_remove
import com.tochlabs.setlist.feature.setlists.resources.add_song_create
import com.tochlabs.setlist.feature.setlists.resources.add_song_no_results
import com.tochlabs.setlist.feature.setlists.resources.add_song_note
import com.tochlabs.setlist.feature.setlists.resources.add_song_present
import com.tochlabs.setlist.feature.setlists.resources.add_song_search
import com.tochlabs.setlist.feature.setlists.resources.item_bpm
import com.tochlabs.setlist.feature.setlists.resources.item_instrument
import com.tochlabs.setlist.feature.setlists.resources.item_key
import com.tochlabs.setlist.feature.setlists.resources.item_link
import com.tochlabs.setlist.feature.setlists.resources.item_link_no_effect
import com.tochlabs.setlist.feature.setlists.resources.item_note
import com.tochlabs.setlist.feature.setlists.resources.item_original
import com.tochlabs.setlist.feature.setlists.resources.item_transition
import com.tochlabs.setlist.feature.setlists.resources.item_tuning
import com.tochlabs.setlist.feature.setlists.resources.stop_added_at_end
import com.tochlabs.setlist.feature.setlists.resources.stop_duration
import com.tochlabs.setlist.feature.setlists.resources.stop_edit
import com.tochlabs.setlist.feature.setlists.resources.stop_minus_minute
import com.tochlabs.setlist.feature.setlists.resources.stop_minus_seconds
import com.tochlabs.setlist.feature.setlists.resources.stop_note
import com.tochlabs.setlist.feature.setlists.resources.stop_plus_minute
import com.tochlabs.setlist.feature.setlists.resources.stop_plus_seconds
import com.tochlabs.setlist.feature.setlists.resources.tuning_standard
import com.tochlabs.setlist.feature.setlists.resources.value_none
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Search the catalogue and add a song; offers to create one when nothing matches. */
@Composable
internal fun AddSongSheet(
    state: AddSongUiState,
    onQueryChange: (String) -> Unit,
    onAdd: (SongResult) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    SetlistSheet(title = stringResource(Res.string.action_add_song), onDismiss = onDismiss) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(Res.string.add_song_search)) },
            leadingIcon = { Icon(painterResource(CoreRes.drawable.ic_search), contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(Res.string.add_song_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.results.isEmpty()) {
            Text(stringResource(Res.string.add_song_no_results), style = MaterialTheme.typography.bodyMedium)
        }
        if (state.offerCreate) {
            Button(onClick = onCreate) { Text(stringResource(Res.string.add_song_create, state.query.trim())) }
        }
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
            items(state.results, key = { it.song.id.value }) { result ->
                ListItem(
                    modifier = Modifier.clickable { onAdd(result) },
                    headlineContent = { Text(result.song.title) },
                    supportingContent = result.song.artist?.let { artist -> { Text(artist) } },
                    trailingContent =
                        if (result.alreadyInSetlist) ({ Tag(stringResource(Res.string.add_song_present)) }) else null,
                )
            }
        }
    }
}

/** New stop (added at the end) or edit of an existing one. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StopSheet(
    initial: StopDraft,
    onSave: (StopDraft) -> Unit,
    onRemove: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    val isNew = initial.itemId == null
    SetlistSheet(
        title = stringResource(if (isNew) Res.string.action_add_stop else Res.string.stop_edit),
        onDismiss = onDismiss,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StopType.entries.forEach { type ->
                FilterChip(
                    selected = draft.type == type,
                    onClick = { draft = draft.withType(type) },
                    label = { Text(stringResource(type.label())) },
                )
            }
        }
        Text(stringResource(Res.string.stop_duration), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperButton("−1:00", stringResource(Res.string.stop_minus_minute)) { draft = draft.adjust(-MINUTE) }
            ActionIcon(CoreRes.drawable.ic_remove, stringResource(Res.string.stop_minus_seconds), {
                draft = draft.adjust(-QUARTER_MINUTE)
            })
            Text(
                formatClock(draft.durationSeconds.toLong()),
                style = MaterialTheme.typography.headlineSmall,
            )
            ActionIcon(CoreRes.drawable.ic_add, stringResource(Res.string.stop_plus_seconds), {
                draft = draft.adjust(QUARTER_MINUTE)
            })
            StepperButton("+1:00", stringResource(Res.string.stop_plus_minute)) { draft = draft.adjust(MINUTE) }
        }
        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.withNote(it) },
            label = { Text(stringResource(Res.string.stop_note)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (isNew) {
            Text(
                stringResource(Res.string.stop_added_at_end),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SheetActions(
            saveLabel = stringResource(if (isNew) CoreRes.string.action_add else CoreRes.string.action_save),
            onSave = { onSave(draft) },
            onRemove = onRemove,
        )
    }
}

@Composable
private fun StepperButton(
    text: String,
    label: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = label }) { Text(text) }
}

/** Overrides for this setlist, note and link to the next song (specs/setlists). */
@Composable
internal fun ItemEditorSheet(
    initial: SongItemDraft,
    onSave: (SongItemDraft) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    val none = stringResource(Res.string.value_none)
    SetlistSheet(title = initial.song.title, onDismiss = onDismiss) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = draft.key,
                onValueChange = { draft = draft.withKey(it) },
                label = { Text(stringResource(Res.string.item_key)) },
                supportingText = { Text(stringResource(Res.string.item_original, draft.originalKey ?: none)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = draft.bpm,
                onValueChange = { draft = draft.withBpm(it) },
                label = { Text(stringResource(Res.string.item_bpm)) },
                supportingText = {
                    Text(stringResource(Res.string.item_original, draft.originalBpm?.toString() ?: none))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        ShortTextField(draft.instrument, stringResource(Res.string.item_instrument), null) {
            draft = draft.withInstrument(it)
        }
        val originalTuning = draft.originalTuning ?: stringResource(Res.string.tuning_standard)
        ShortTextField(draft.tuning, stringResource(Res.string.item_tuning), originalTuning) {
            draft = draft.withTuning(it)
        }
        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.withNote(it) },
            label = { Text(stringResource(Res.string.item_note)) },
            modifier = Modifier.fillMaxWidth(),
        )
        LinkFields(draft) { draft = it }
        SheetActions(
            saveLabel = stringResource(CoreRes.string.action_save),
            onSave = { onSave(draft) },
            onRemove = onRemove,
        )
    }
}

@Composable
private fun LinkFields(
    draft: SongItemDraft,
    onChange: (SongItemDraft) -> Unit,
) {
    // The whole row toggles the link, not only the small switch.
    Row(
        modifier =
            Modifier.fillMaxWidth().toggleable(
                value = draft.linked,
                role = Role.Switch,
                onValueChange = { onChange(draft.withLinked(it)) },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.item_link), modifier = Modifier.weight(1f))
        Switch(checked = draft.linked, onCheckedChange = null)
    }
    if (!draft.linked) return
    OutlinedTextField(
        value = draft.transition,
        onValueChange = { onChange(draft.withTransition(it)) },
        label = { Text(stringResource(Res.string.item_transition)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    if (!draft.nextIsSong) {
        Text(
            stringResource(Res.string.item_link_no_effect),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Free text of at most 20 characters, with a counter and, when given, the original value. */
@Composable
private fun ShortTextField(
    value: String,
    label: String,
    original: String?,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        supportingText = {
            Row {
                Text(
                    original?.let { stringResource(Res.string.item_original, it) }.orEmpty(),
                    modifier = Modifier.weight(1f),
                )
                Text("${value.length} / $SHORT_TEXT_MAX_LENGTH")
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SheetActions(
    saveLabel: String,
    onSave: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
        if (onRemove != null) {
            OutlinedButton(onClick = onRemove) {
                Text(stringResource(Res.string.action_remove), color = MaterialTheme.colorScheme.error)
            }
        }
        Button(onClick = onSave) { Text(saveLabel) }
    }
}

private const val MINUTE = 60
private const val QUARTER_MINUTE = 15
