package com.tochlabs.setlist.feature.bands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BackButton
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.MessageEffect
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.ic_delete
import com.tochlabs.setlist.core.ui.resources.ic_unarchive
import com.tochlabs.setlist.feature.bands.resources.Res
import com.tochlabs.setlist.feature.bands.resources.band_delete
import com.tochlabs.setlist.feature.bands.resources.band_deleted
import com.tochlabs.setlist.feature.bands.resources.band_restore
import com.tochlabs.setlist.feature.bands.resources.band_restored
import com.tochlabs.setlist.feature.bands.resources.bands_history
import com.tochlabs.setlist.feature.bands.resources.delete_body
import com.tochlabs.setlist.feature.bands.resources.delete_songs_stay
import com.tochlabs.setlist.feature.bands.resources.delete_title
import com.tochlabs.setlist.feature.bands.resources.events_count
import com.tochlabs.setlist.feature.bands.resources.history_empty_title
import com.tochlabs.setlist.feature.bands.resources.history_note
import com.tochlabs.setlist.feature.bands.resources.repertoire_count
import com.tochlabs.setlist.feature.bands.resources.setlists_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Archived bands with restore and permanent deletion (specs/bands). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandHistoryScreen(
    onBack: () -> Unit,
    viewModel: BandHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message =
        when (val m = state.message) {
            is HistoryMessage.Restored -> stringResource(Res.string.band_restored, m.bandName)
            is HistoryMessage.Deleted -> stringResource(Res.string.band_deleted, m.bandName)
            null -> null
        }
    MessageEffect(message, snackbar, viewModel::messageShown)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.bands_history)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.loading -> Unit
            state.bands.isEmpty() ->
                EmptyState(
                    title = stringResource(Res.string.history_empty_title),
                    modifier = Modifier.padding(padding),
                )
            else ->
                HistoryList(
                    bands = state.bands,
                    onRestore = viewModel::restore,
                    onDelete = viewModel::requestDelete,
                    modifier = Modifier.padding(padding),
                )
        }
    }

    state.pendingDeletion?.let { pending ->
        DeleteBandDialog(pending, onConfirm = viewModel::confirmDelete, onDismiss = viewModel::cancelDelete)
    }
}

@Composable
private fun HistoryList(
    bands: List<Band>,
    onRestore: (BandId) -> Unit,
    onDelete: (BandId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Text(
                stringResource(Res.string.history_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(bands, key = { it.id.value }) { band ->
            ListItem(
                leadingContent = { BandAvatar(band) },
                headlineContent = { Text(band.name) },
                trailingContent = {
                    Row {
                        ActionIcon(CoreRes.drawable.ic_unarchive, stringResource(Res.string.band_restore)) {
                            onRestore(band.id)
                        }
                        ActionIcon(CoreRes.drawable.ic_delete, stringResource(Res.string.band_delete)) {
                            onDelete(band.id)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun DeleteBandDialog(
    pending: PendingDeletion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val summary = pending.summary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_title, pending.band.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.delete_body))
                Text("• " + pluralStringResource(Res.plurals.setlists_count, summary.setlists, summary.setlists))
                Text("• " + pluralStringResource(Res.plurals.events_count, summary.events, summary.events))
                Text(
                    "• " +
                        pluralStringResource(
                            Res.plurals.repertoire_count,
                            summary.repertoireEntries,
                            summary.repertoireEntries,
                        ),
                )
                Text(stringResource(Res.string.delete_songs_stay), modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.band_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreRes.string.action_cancel)) } },
    )
}
