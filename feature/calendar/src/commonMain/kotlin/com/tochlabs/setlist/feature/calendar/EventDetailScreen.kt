package com.tochlabs.setlist.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BackButton
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.Tag
import com.tochlabs.setlist.core.ui.component.TagTone
import com.tochlabs.setlist.core.ui.format.currentUiLanguage
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.format.durationText
import com.tochlabs.setlist.core.ui.format.formatTimeRange
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.format.milestoneLabel
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_delete
import com.tochlabs.setlist.core.ui.resources.ic_delete
import com.tochlabs.setlist.core.ui.resources.ic_edit
import com.tochlabs.setlist.feature.calendar.resources.Res
import com.tochlabs.setlist.feature.calendar.resources.action_edit
import com.tochlabs.setlist.feature.calendar.resources.event_address
import com.tochlabs.setlist.feature.calendar.resources.event_contact
import com.tochlabs.setlist.feature.calendar.resources.event_delete
import com.tochlabs.setlist.feature.calendar.resources.event_delete_body
import com.tochlabs.setlist.feature.calendar.resources.event_delete_title
import com.tochlabs.setlist.feature.calendar.resources.event_parking
import com.tochlabs.setlist.feature.calendar.resources.event_setlist
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_change
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_incomplete
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_link
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_none
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_open
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_total
import com.tochlabs.setlist.feature.calendar.resources.event_setlist_unlink
import com.tochlabs.setlist.feature.calendar.resources.event_venue
import com.tochlabs.setlist.feature.calendar.resources.logistics
import com.tochlabs.setlist.feature.calendar.resources.logistics_empty
import com.tochlabs.setlist.feature.calendar.resources.milestone_add
import com.tochlabs.setlist.feature.calendar.resources.roadmap
import com.tochlabs.setlist.feature.calendar.resources.roadmap_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Which sheet is open on the event screen. */
private sealed interface EventSheet {
    data class Milestone(
        val draft: MilestoneDraft,
    ) : EventSheet

    data object PickSetlist : EventSheet
}

/** Event sheet: logistics, roadmap and linked setlist (specs/event-calendar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: EventId,
    onBack: () -> Unit,
    onEdit: (EventId) -> Unit,
    onOpenSetlist: (SetlistId) -> Unit,
    viewModel: EventDetailViewModel =
        koinViewModel(key = "event-detail-${eventId.value}") { parametersOf(eventId.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<EventSheet?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    // Deleted, or hidden because its band was archived (specs/event-calendar, "Archived bands").
    val gone = state.deleted || (!state.loading && (state.event == null || state.band == null))
    LaunchedEffect(gone) { if (gone) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.event?.title.orEmpty(), maxLines = 1) },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    ActionIcon(CoreRes.drawable.ic_edit, stringResource(Res.string.action_edit)) { onEdit(eventId) }
                    ActionIcon(CoreRes.drawable.ic_delete, stringResource(Res.string.event_delete)) {
                        confirmDelete = true
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading || state.event == null) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(state)
            Logistics(state)
            Roadmap(
                milestones = state.milestones,
                onAdd = { sheet = EventSheet.Milestone(MilestoneDraft()) },
                onOpen = { sheet = EventSheet.Milestone(MilestoneDraft.of(it)) },
            )
            SetlistSection(
                state = state,
                onOpen = onOpenSetlist,
                onPick = { sheet = EventSheet.PickSetlist },
                onUnlink = { viewModel.linkSetlist(null) },
            )
        }
    }

    EventSheets(sheet, state, viewModel) { sheet = null }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = state.event?.title.orEmpty(),
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
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
        title = { Text(stringResource(Res.string.event_delete_title, title)) },
        text = { Text(stringResource(Res.string.event_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(CoreRes.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreRes.string.action_cancel)) } },
    )
}

@Composable
private fun EventSheets(
    sheet: EventSheet?,
    state: EventDetailUiState,
    viewModel: EventDetailViewModel,
    onClose: () -> Unit,
) {
    when (sheet) {
        null -> Unit
        is EventSheet.Milestone ->
            MilestoneSheet(
                initial = sheet.draft,
                onSave = {
                    viewModel.saveMilestone(it)
                    onClose()
                },
                onRemove =
                    sheet.draft.id?.let { id ->
                        {
                            viewModel.deleteMilestone(id)
                            onClose()
                        }
                    },
                onDismiss = onClose,
            )
        EventSheet.PickSetlist ->
            PickSetlistSheet(
                setlists = state.linkableSetlists,
                onPick = {
                    viewModel.linkSetlist(it)
                    onClose()
                },
                onDismiss = onClose,
            )
    }
}

@Composable
private fun Header(state: EventDetailUiState) {
    val event = state.event ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        state.band?.let { BandDot(it.color) }
        val parts = listOfNotNull(state.band?.name, stringResource(event.type.label()), dateText(event.date))
        Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** Venue, address, parking and contact. The MVP does not open the address in a maps app. */
@Composable
private fun Logistics(state: EventDetailUiState) {
    val event = state.event ?: return
    Section(stringResource(Res.string.logistics)) {
        val rows =
            listOfNotNull(
                event.venue?.let { Res.string.event_venue to it },
                event.address?.let { Res.string.event_address to it },
                event.parking?.let { Res.string.event_parking to it },
                event.contact?.let { Res.string.event_contact to it },
            )
        if (rows.isEmpty()) Muted(stringResource(Res.string.logistics_empty))
        rows.forEach { (label, value) ->
            Column {
                Text(
                    stringResource(label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun Roadmap(
    milestones: List<EventMilestone>,
    onAdd: () -> Unit,
    onOpen: (EventMilestone) -> Unit,
) {
    Section(stringResource(Res.string.roadmap)) {
        if (milestones.isEmpty()) Muted(stringResource(Res.string.roadmap_empty))
        val language = currentUiLanguage()
        milestones.forEach { milestone ->
            // Time above the name: 12-hour ranges such as "10:30 PM – 11:45 PM" do not fit in a column.
            Column(Modifier.fillMaxWidth().clickable { onOpen(milestone) }.padding(vertical = 6.dp)) {
                Text(
                    formatTimeRange(milestone.startTime, milestone.endTime, language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(milestoneLabel(milestone), style = MaterialTheme.typography.bodyLarge)
            }
        }
        OutlinedButton(onClick = onAdd) { Text(stringResource(Res.string.milestone_add)) }
    }
}

@Composable
private fun SetlistSection(
    state: EventDetailUiState,
    onOpen: (SetlistId) -> Unit,
    onPick: () -> Unit,
    onUnlink: () -> Unit,
) {
    Section(stringResource(Res.string.event_setlist)) {
        val linked = state.linkedSetlist
        if (linked == null) {
            Muted(stringResource(Res.string.event_setlist_none))
            OutlinedButton(onClick = onPick) { Text(stringResource(Res.string.event_setlist_link)) }
            return@Section
        }
        Text(linked.setlist.name, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.event_setlist_total, durationText(linked.duration.totalWithStopsSeconds)),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (!linked.duration.isComplete) {
                Tag(stringResource(Res.string.event_setlist_incomplete), tone = TagTone.WARNING)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onOpen(linked.setlist.id) },
            ) { Text(stringResource(Res.string.event_setlist_open)) }
            TextButton(onClick = onPick) { Text(stringResource(Res.string.event_setlist_change)) }
            TextButton(onClick = onUnlink) { Text(stringResource(Res.string.event_setlist_unlink)) }
        }
    }
}

@Composable
internal fun Muted(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
