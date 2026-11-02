package com.tochlabs.setlist.feature.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.share.resources.Res
import com.tochlabs.setlist.feature.share.resources.action_close
import com.tochlabs.setlist.feature.share.resources.action_import
import com.tochlabs.setlist.feature.share.resources.import_band
import com.tochlabs.setlist.feature.share.resources.import_band_required
import com.tochlabs.setlist.feature.share.resources.import_from
import com.tochlabs.setlist.feature.share.resources.import_newer
import com.tochlabs.setlist.feature.share.resources.import_no_bands
import com.tochlabs.setlist.feature.share.resources.import_note
import com.tochlabs.setlist.feature.share.resources.import_title
import com.tochlabs.setlist.feature.share.resources.import_unreadable
import com.tochlabs.setlist.feature.share.resources.share_songs
import com.tochlabs.setlist.feature.share.resources.share_stops
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/**
 * Import the `.setlist` file waiting to be opened: shows what it contains, the band to import into,
 * or why it cannot be imported. [onImported] receives the new setlist.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onImported: (SetlistId) -> Unit,
    onClose: () -> Unit,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.importedId) { state.importedId?.let(onImported) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.import_title)) },
                navigationIcon = {
                    ActionIcon(CoreRes.drawable.ic_close, stringResource(CoreRes.string.action_cancel), onClose)
                },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val error = state.error
            val preview = state.preview
            if (error != null || preview == null) {
                Text(
                    stringResource(
                        if (error ==
                            ImportError.NEWER_VERSION
                        ) {
                            Res.string.import_newer
                        } else {
                            Res.string.import_unreadable
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(onClick = onClose) { Text(stringResource(Res.string.action_close)) }
                return@Column
            }
            PreviewCard(preview)
            Text(
                stringResource(Res.string.import_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BandChoice(state, viewModel)
        }
    }
}

@Composable
private fun PreviewCard(preview: ImportPreview) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(preview.setlistName, style = MaterialTheme.typography.titleLarge)
            if (preview.bandName.isNotEmpty()) {
                Text(
                    stringResource(Res.string.import_from, preview.bandName),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                listOf(
                    pluralStringResource(Res.plurals.share_songs, preview.songCount, preview.songCount.toString()),
                    pluralStringResource(Res.plurals.share_stops, preview.stopCount, preview.stopCount.toString()),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BandChoice(
    state: ImportUiState,
    viewModel: ImportViewModel,
) {
    if (state.bands.isEmpty()) {
        Text(stringResource(Res.string.import_no_bands), color = MaterialTheme.colorScheme.error)
        return
    }
    Text(stringResource(Res.string.import_band), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.bands.forEach { band ->
            FilterChip(
                selected = band.id == state.bandId,
                onClick = { viewModel.selectBand(band.id) },
                label = { Text(band.name) },
                leadingIcon = { BandDot(band.color) },
            )
        }
    }
    if (state.bandError) {
        Text(
            stringResource(Res.string.import_band_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Button(onClick = viewModel::import, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.action_import))
    }
}
