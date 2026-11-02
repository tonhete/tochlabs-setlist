package com.tochlabs.setlist.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.tochlabs.setlist.core.domain.calendar.STANDARD_MILESTONES
import com.tochlabs.setlist.core.domain.validation.ValidationError
import com.tochlabs.setlist.core.model.MilestoneKind
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.SetlistSheet
import com.tochlabs.setlist.core.ui.component.TimePickerDialogFor
import com.tochlabs.setlist.core.ui.format.currentUiLanguage
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.format.formatTime
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.feature.calendar.resources.Res
import com.tochlabs.setlist.feature.calendar.resources.error_end_before_start
import com.tochlabs.setlist.feature.calendar.resources.error_label_required
import com.tochlabs.setlist.feature.calendar.resources.error_start_required
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_link
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_none_available
import com.tochlabs.setlist.feature.calendar.resources.milestone_add
import com.tochlabs.setlist.feature.calendar.resources.milestone_choose_time
import com.tochlabs.setlist.feature.calendar.resources.milestone_edit
import com.tochlabs.setlist.feature.calendar.resources.milestone_end
import com.tochlabs.setlist.feature.calendar.resources.milestone_has_end
import com.tochlabs.setlist.feature.calendar.resources.milestone_name
import com.tochlabs.setlist.feature.calendar.resources.milestone_name_hint
import com.tochlabs.setlist.feature.calendar.resources.milestone_remove
import com.tochlabs.setlist.feature.calendar.resources.milestone_start
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Which time a picker is open for. */
private enum class TimeField { START, END }

/** Add or edit a milestone: kind (the standard ones plus custom), name, start and optional end. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MilestoneSheet(
    initial: MilestoneDraft,
    onSave: (MilestoneDraft) -> Unit,
    onRemove: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    var picking by remember { mutableStateOf<TimeField?>(null) }
    // Errors show once the user tries to save, not while the sheet is still empty.
    var tried by remember { mutableStateOf(false) }
    SetlistSheet(
        title = stringResource(if (initial.id == null) Res.string.milestone_add else Res.string.milestone_edit),
        onDismiss = onDismiss,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (STANDARD_MILESTONES + MilestoneKind.CUSTOM).forEach { kind ->
                FilterChip(
                    selected = draft.kind == kind,
                    onClick = { draft = draft.withKind(kind) },
                    label = { Text(stringResource(kind.label())) },
                )
            }
        }
        val standardName = stringResource(draft.kind.label())
        OutlinedTextField(
            value = draft.label,
            onValueChange = { draft = draft.withLabel(it) },
            label = { Text(stringResource(Res.string.milestone_name)) },
            placeholder = if (draft.kind != MilestoneKind.CUSTOM) ({ Text(standardName) }) else null,
            supportingText =
                if (draft.kind != MilestoneKind.CUSTOM) {
                    { Text(stringResource(Res.string.milestone_name_hint, standardName)) }
                } else {
                    null
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        TimeRow(stringResource(Res.string.milestone_start), draft.start) { picking = TimeField.START }
        HasEndRow(draft.hasEnd) { hasEnd ->
            draft = if (hasEnd) draft.copy(hasEnd = true) else draft.withoutEnd()
        }
        if (draft.hasEnd) TimeRow(stringResource(Res.string.milestone_end), draft.end) { picking = TimeField.END }
        draft.error?.takeIf { tried }?.let { ErrorText(stringResource(it.message())) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            if (onRemove != null) {
                OutlinedButton(onClick = onRemove) {
                    Text(stringResource(Res.string.milestone_remove), color = MaterialTheme.colorScheme.error)
                }
            }
            Button(onClick = {
                tried = true
                if (draft.error == null) onSave(draft)
            }) { Text(stringResource(CoreRes.string.action_save)) }
        }
    }
    picking?.let { field ->
        TimePickerDialogFor(
            initial = if (field == TimeField.START) draft.start else draft.end ?: draft.start,
            onPicked = { draft = if (field == TimeField.START) draft.withStart(it) else draft.withEnd(it) },
            onDismiss = { picking = null },
        )
    }
}

private fun ValidationError.message() =
    when (this) {
        ValidationError.END_BEFORE_START -> Res.string.error_end_before_start
        ValidationError.LABEL_REQUIRED -> Res.string.error_label_required
        else -> Res.string.error_start_required
    }

@Composable
private fun TimeRow(
    label: String,
    time: LocalTime?,
    onPick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onPick) {
            Text(time?.let { formatTime(it, currentUiLanguage()) } ?: stringResource(Res.string.milestone_choose_time))
        }
    }
}

/** The whole row toggles the end time, not only the small switch. */
@Composable
private fun HasEndRow(
    hasEnd: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = hasEnd, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.milestone_has_end), modifier = Modifier.weight(1f))
        Switch(checked = hasEnd, onCheckedChange = null)
    }
}

/** Setlists of the event's band that are not linked to another event. */
@Composable
internal fun PickSetlistSheet(
    setlists: List<Setlist>,
    onPick: (SetlistId) -> Unit,
    onDismiss: () -> Unit,
) {
    SetlistSheet(title = stringResource(Res.string.event_setlist_link), onDismiss = onDismiss) {
        if (setlists.isEmpty()) Muted(stringResource(Res.string.event_setlist_none_available))
        setlists.forEach { setlist ->
            ListItem(
                modifier = Modifier.clickable { onPick(setlist.id) },
                headlineContent = { Text(setlist.name) },
                supportingContent = setlist.date?.let { date -> { Text(dateText(date)) } },
            )
        }
    }
}
