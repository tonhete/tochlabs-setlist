package com.tochlabs.setlist.feature.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.MessageEffect
import com.tochlabs.setlist.core.ui.component.SettingsAction
import com.tochlabs.setlist.core.ui.component.toColor
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_archive
import com.tochlabs.setlist.core.ui.resources.ic_history
import com.tochlabs.setlist.core.ui.resources.tab_bands
import com.tochlabs.setlist.feature.bands.resources.Res
import com.tochlabs.setlist.feature.bands.resources.band_archive
import com.tochlabs.setlist.feature.bands.resources.band_archived
import com.tochlabs.setlist.feature.bands.resources.band_new
import com.tochlabs.setlist.feature.bands.resources.bands_empty_body
import com.tochlabs.setlist.feature.bands.resources.bands_empty_title
import com.tochlabs.setlist.feature.bands.resources.bands_history
import com.tochlabs.setlist.feature.bands.resources.setlists_count
import com.tochlabs.setlist.feature.bands.resources.songs_count
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Bands tab root: active bands, archive action, entry to the history. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandsScreen(
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddBand: () -> Unit,
    onEditBand: (BandId) -> Unit,
    viewModel: BandsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message =
        when (val m = state.message) {
            is BandsMessage.Archived -> stringResource(Res.string.band_archived, m.bandName)
            null -> null
        }
    MessageEffect(message, snackbar, viewModel::messageShown)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.tab_bands)) },
                actions = {
                    if (state.hasHistory) {
                        ActionIcon(CoreRes.drawable.ic_history, stringResource(Res.string.bands_history), onOpenHistory)
                    }
                    SettingsAction(onOpenSettings)
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBand) {
                Icon(painterResource(CoreRes.drawable.ic_add), stringResource(Res.string.band_new))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.loading -> Unit
            state.bands.isEmpty() ->
                EmptyState(
                    title = stringResource(Res.string.bands_empty_title),
                    body = stringResource(Res.string.bands_empty_body),
                    modifier = Modifier.padding(padding),
                )
            else ->
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(state.bands, key = { it.band.id.value }) { row ->
                        BandRowItem(
                            row,
                            onClick = { onEditBand(row.band.id) },
                            onArchive = { viewModel.archive(row.band.id) },
                        )
                    }
                }
        }
    }
}

@Composable
private fun BandRowItem(
    row: BandRow,
    onClick: () -> Unit,
    onArchive: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { BandAvatar(row.band) },
        headlineContent = { Text(row.band.name) },
        supportingContent = {
            Text(
                pluralStringResource(Res.plurals.songs_count, row.songCount, row.songCount) + " · " +
                    pluralStringResource(Res.plurals.setlists_count, row.setlistCount, row.setlistCount),
            )
        },
        trailingContent = {
            ActionIcon(
                CoreRes.drawable.ic_archive,
                stringResource(Res.string.band_archive),
                onArchive,
            )
        },
    )
}

/** Circle in the band colour with the band's initial. */
@Composable
internal fun BandAvatar(band: Band) {
    Box(
        modifier = Modifier.size(40.dp).background(band.color.toColor(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = band.name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}
