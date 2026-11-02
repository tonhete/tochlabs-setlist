package com.tochlabs.setlist.feature.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.BackButton
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.feature.share.resources.Res
import com.tochlabs.setlist.feature.share.resources.action_print
import com.tochlabs.setlist.feature.share.resources.action_share
import com.tochlabs.setlist.feature.share.resources.export_band_file
import com.tochlabs.setlist.feature.share.resources.export_band_file_desc
import com.tochlabs.setlist.feature.share.resources.export_list_pdf
import com.tochlabs.setlist.feature.share.resources.export_list_pdf_desc
import com.tochlabs.setlist.feature.share.resources.export_missing
import com.tochlabs.setlist.feature.share.resources.export_stage_pdf
import com.tochlabs.setlist.feature.share.resources.export_stage_pdf_desc
import com.tochlabs.setlist.feature.share.resources.export_title
import com.tochlabs.setlist.feature.share.resources.share_songs
import com.tochlabs.setlist.feature.share.resources.share_stops
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stage PDF, list PDF or file for the band, then share or print (docs/product/screens-and-flows.md). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    setlistId: SetlistId,
    onBack: () -> Unit,
    viewModel: ExportViewModel = koinViewModel(key = "export-${setlistId.value}") { parametersOf(setlistId.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = rememberDocumentActions()
    LaunchedEffect(state.ready) {
        val ready = state.ready ?: return@LaunchedEffect
        when (ready.action) {
            ExportAction.SHARE -> actions.share(ready.file)
            ExportAction.PRINT -> actions.print(ready.file)
        }
        viewModel.consumed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.export_title)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        if (state.missing) {
            EmptyState(title = stringResource(Res.string.export_missing), modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Summary(state)
            ExportMode.entries.forEach { mode ->
                ModeCard(mode, selected = state.mode == mode) { viewModel.selectMode(mode) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::share, enabled = !state.busy, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.action_share))
                }
                if (state.canPrint) {
                    OutlinedButton(onClick = viewModel::print, enabled = !state.busy, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.action_print))
                    }
                }
            }
        }
    }
}

@Composable
private fun Summary(state: ExportUiState) {
    Text(state.setlistName, style = MaterialTheme.typography.titleLarge)
    val parts =
        listOf(
            state.bandName,
            pluralStringResource(Res.plurals.share_songs, state.songCount, state.songCount.toString()),
            pluralStringResource(Res.plurals.share_stops, state.stopCount, state.stopCount.toString()),
        )
    Text(
        parts.filter { it.isNotEmpty() }.joinToString(" · "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun ExportMode.texts(): Pair<StringResource, StringResource> =
    when (this) {
        ExportMode.STAGE_PDF -> Res.string.export_stage_pdf to Res.string.export_stage_pdf_desc
        ExportMode.LIST_PDF -> Res.string.export_list_pdf to Res.string.export_list_pdf_desc
        ExportMode.BAND_FILE -> Res.string.export_band_file to Res.string.export_band_file_desc
    }

@Composable
private fun ModeCard(
    mode: ExportMode,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val (title, description) = mode.texts()
    val colors =
        if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    Card(
        colors = colors,
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(description), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
