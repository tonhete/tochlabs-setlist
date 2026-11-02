package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BackButton
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.MessageEffect
import com.tochlabs.setlist.core.ui.component.rememberReorderState
import com.tochlabs.setlist.core.ui.component.reorderableItems
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_delete
import com.tochlabs.setlist.core.ui.resources.ic_delete
import com.tochlabs.setlist.core.ui.resources.ic_edit
import com.tochlabs.setlist.core.ui.resources.ic_play
import com.tochlabs.setlist.core.ui.resources.ic_reorder
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.action_add_song
import com.tochlabs.setlist.feature.setlists.resources.action_add_stop
import com.tochlabs.setlist.feature.setlists.resources.action_done
import com.tochlabs.setlist.feature.setlists.resources.action_edit
import com.tochlabs.setlist.feature.setlists.resources.action_reorder
import com.tochlabs.setlist.feature.setlists.resources.action_stage
import com.tochlabs.setlist.feature.setlists.resources.added
import com.tochlabs.setlist.feature.setlists.resources.added_repeated
import com.tochlabs.setlist.feature.setlists.resources.detail_empty_body
import com.tochlabs.setlist.feature.setlists.resources.detail_empty_title
import com.tochlabs.setlist.feature.setlists.resources.setlist_delete
import com.tochlabs.setlist.feature.setlists.resources.setlist_delete_body
import com.tochlabs.setlist.feature.setlists.resources.setlist_delete_body_event
import com.tochlabs.setlist.feature.setlists.resources.setlist_delete_title
import com.tochlabs.setlist.feature.setlists.resources.setlist_linked_event
import com.tochlabs.setlist.feature.setlists.resources.setlist_no_date
import com.tochlabs.setlist.feature.setlists.resources.stage_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Which sheet is open on the detail screen. */
private sealed interface DetailSheet {
    data object AddSong : DetailSheet

    data class Stop(
        val draft: StopDraft,
    ) : DetailSheet

    data class SongItem(
        val draft: SongItemDraft,
    ) : DetailSheet
}

/** One setlist: total card, pause, items, reorder mode, sheets and deletion. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailScreen(
    setlistId: SetlistId,
    onBack: () -> Unit,
    onEdit: (SetlistId) -> Unit,
    onOpenStage: (SetlistId) -> Unit,
    viewModel: SetlistDetailViewModel =
        koinViewModel(key = "setlist-detail-${setlistId.value}") { parametersOf(setlistId.value) },
    addSong: AddSongViewModel = koinViewModel(key = "add-song-${setlistId.value}") { parametersOf(setlistId.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val addSongState by addSong.state.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<DetailSheet?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showStageEmpty by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    // Deleted here, no longer there (deleted with its band), or hidden because its band was archived.
    val gone = state.deleted || (!state.loading && (state.setlist == null || state.band == null))
    LaunchedEffect(gone) { if (gone) onBack() }

    DetailMessages(addSongState.message, addSong::messageShown, showStageEmpty, snackbar) { showStageEmpty = false }

    Scaffold(
        topBar = {
            DetailTopBar(
                state = state,
                actions =
                    DetailActions(
                        onBack = onBack,
                        onEdit = { onEdit(setlistId) },
                        onToggleReorder = viewModel::toggleReorderMode,
                        onDelete = { confirmDelete = true },
                        onStage = { if (state.canOpenStage) onOpenStage(setlistId) else showStageEmpty = true },
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!state.reorderMode) {
                AddBar(
                    onAddSong = {
                        addSong.onQueryChange("")
                        sheet = DetailSheet.AddSong
                    },
                    onAddStop = { sheet = DetailSheet.Stop(StopDraft()) },
                )
            }
        },
    ) { padding ->
        if (state.loading || state.setlist == null) return@Scaffold
        DetailContent(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.padding(padding),
            onOpenItem = { row ->
                sheet =
                    row.songDraft()?.let { DetailSheet.SongItem(it) } ?: row.stopDraft()?.let { DetailSheet.Stop(it) }
            },
        )
    }

    DetailSheets(sheet, addSongState, addSong, viewModel) { sheet = null }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            state,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

/** Snackbars: song added (with the duplicate warning) and "the setlist is empty" for stage mode. */
@Composable
private fun DetailMessages(
    added: AddSongMessage?,
    onAddedShown: () -> Unit,
    stageEmpty: Boolean,
    snackbar: SnackbarHostState,
    onStageEmptyShown: () -> Unit,
) {
    val addedText =
        added?.let { stringResource(if (it.repeated) Res.string.added_repeated else Res.string.added, it.title) }
    MessageEffect(addedText, snackbar, onAddedShown)
    MessageEffect(if (stageEmpty) stringResource(Res.string.stage_empty) else null, snackbar, onStageEmptyShown)
}

private class DetailActions(
    val onBack: () -> Unit,
    val onEdit: () -> Unit,
    val onToggleReorder: () -> Unit,
    val onDelete: () -> Unit,
    val onStage: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
    state: SetlistDetailUiState,
    actions: DetailActions,
) {
    TopAppBar(
        title = { Text(state.setlist?.name.orEmpty(), maxLines = 1) },
        navigationIcon = { BackButton(actions.onBack) },
        actions = {
            if (state.reorderMode) {
                TextButton(onClick = actions.onToggleReorder) { Text(stringResource(Res.string.action_done)) }
            } else {
                ActionIcon(CoreRes.drawable.ic_play, stringResource(Res.string.action_stage), actions.onStage)
                if (state.rows.size > 1) {
                    ActionIcon(
                        CoreRes.drawable.ic_reorder,
                        stringResource(Res.string.action_reorder),
                        actions.onToggleReorder,
                    )
                }
                ActionIcon(CoreRes.drawable.ic_edit, stringResource(Res.string.action_edit), actions.onEdit)
                ActionIcon(CoreRes.drawable.ic_delete, stringResource(Res.string.setlist_delete), actions.onDelete)
            }
        },
    )
}

@Composable
private fun AddBar(
    onAddSong: () -> Unit,
    onAddStop: () -> Unit,
) {
    BottomAppBar {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onAddSong,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(Res.string.action_add_song)) }
            FilledTonalButton(onClick = onAddStop, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.action_add_stop))
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: SetlistDetailUiState,
    viewModel: SetlistDetailViewModel,
    modifier: Modifier,
    onOpenItem: (ItemRow) -> Unit,
) {
    val listState = rememberLazyListState()
    val reorder =
        rememberReorderState(
            listState,
            onMove = { from, to -> viewModel.onDragMove(SetlistItemId(from), SetlistItemId(to)) },
            onDragEnd = viewModel::onDragEnd,
        )
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(key = "header") {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Header(state)
                TotalCard(state, viewModel::setTotalMode)
                PauseStepper(state, viewModel::decreasePause, viewModel::increasePause)
            }
        }
        if (state.rows.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = stringResource(Res.string.detail_empty_title),
                    body = stringResource(Res.string.detail_empty_body),
                )
            }
        }
        reorderableItems(reorder, state.rows, key = { it.id.value }) { row, isDragging ->
            ItemRowView(
                row = row,
                reorderMode = state.reorderMode,
                isDragging = isDragging,
                onClick = { onOpenItem(row) },
                onMoveUp = { viewModel.moveUp(row.id) },
                onMoveDown = { viewModel.moveDown(row.id) },
            )
        }
    }
}

@Composable
private fun Header(state: SetlistDetailUiState) {
    val setlist = state.setlist ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        state.band?.let { BandDot(it.color) }
        val date = setlist.date?.let { dateText(it) } ?: stringResource(Res.string.setlist_no_date)
        Text(listOfNotNull(state.band?.name, date).joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
    }
    state.event?.let {
        Text(
            stringResource(Res.string.setlist_linked_event, it.title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DetailSheets(
    sheet: DetailSheet?,
    addSongState: AddSongUiState,
    addSong: AddSongViewModel,
    viewModel: SetlistDetailViewModel,
    onClose: () -> Unit,
) {
    when (sheet) {
        null -> Unit
        DetailSheet.AddSong ->
            AddSongSheet(
                state = addSongState,
                onQueryChange = addSong::onQueryChange,
                onAdd = {
                    addSong.add(it.song)
                    onClose()
                },
                onCreate = {
                    addSong.createAndAdd()
                    onClose()
                },
                onDismiss = onClose,
            )
        is DetailSheet.Stop -> {
            val id = sheet.draft.itemId
            StopSheet(
                initial = sheet.draft,
                onSave = {
                    if (id == null) viewModel.addStop(it) else viewModel.saveStop(it)
                    onClose()
                },
                onRemove =
                    id?.let {
                        {
                            viewModel.removeItem(it)
                            onClose()
                        }
                    },
                onDismiss = onClose,
            )
        }
        is DetailSheet.SongItem ->
            ItemEditorSheet(
                initial = sheet.draft,
                onSave = {
                    viewModel.saveSongItem(it)
                    onClose()
                },
                onRemove = {
                    viewModel.removeItem(sheet.draft.item.id)
                    onClose()
                },
                onDismiss = onClose,
            )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    state: SetlistDetailUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val event = state.event
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.setlist_delete_title, state.setlist?.name.orEmpty())) },
        text = {
            Text(
                if (event == null) {
                    stringResource(Res.string.setlist_delete_body)
                } else {
                    stringResource(Res.string.setlist_delete_body_event, event.title)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(CoreRes.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreRes.string.action_cancel)) } },
        modifier = Modifier.navigationBarsPadding(),
    )
}
