package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.BandFilterChips
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.SettingsAction
import com.tochlabs.setlist.core.ui.component.Tag
import com.tochlabs.setlist.core.ui.component.TagTone
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.format.durationText
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_import
import com.tochlabs.setlist.core.ui.resources.tab_setlists
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.action_import_file
import com.tochlabs.setlist.feature.setlists.resources.setlist_has_roadmap
import com.tochlabs.setlist.feature.setlists.resources.setlist_incomplete
import com.tochlabs.setlist.feature.setlists.resources.setlist_no_date
import com.tochlabs.setlist.feature.setlists.resources.setlist_songs
import com.tochlabs.setlist.feature.setlists.resources.setlists_empty_body
import com.tochlabs.setlist.feature.setlists.resources.setlists_empty_title
import com.tochlabs.setlist.feature.setlists.resources.setlists_hide_past
import com.tochlabs.setlist.feature.setlists.resources.setlists_new
import com.tochlabs.setlist.feature.setlists.resources.setlists_no_bands_body
import com.tochlabs.setlist.feature.setlists.resources.setlists_no_bands_title
import com.tochlabs.setlist.feature.setlists.resources.setlists_show_past
import com.tochlabs.setlist.feature.setlists.resources.setlists_upcoming
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Setlists tab root: upcoming setlists, the collapsible past group and the band filter. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistsScreen(
    onOpenSettings: () -> Unit,
    onAddSetlist: (preselectedBand: BandId?) -> Unit,
    onOpenSetlist: (SetlistId) -> Unit,
    /** Opens the file picker to import a `.setlist` file. */
    onImport: () -> Unit,
    viewModel: SetlistsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.tab_setlists)) },
                actions = {
                    if (!state.noBands) {
                        ActionIcon(CoreRes.drawable.ic_import, stringResource(Res.string.action_import_file), onImport)
                    }
                    SettingsAction(onOpenSettings)
                },
            )
        },
        floatingActionButton = {
            if (!state.loading && !state.noBands) {
                FloatingActionButton(onClick = { onAddSetlist(state.selectedBand) }) {
                    Icon(painterResource(CoreRes.drawable.ic_add), stringResource(Res.string.setlists_new))
                }
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        if (state.noBands) {
            EmptyState(
                title = stringResource(Res.string.setlists_no_bands_title),
                body = stringResource(Res.string.setlists_no_bands_body),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            BandFilterChips(state.bands, state.selectedBand, viewModel::selectBand)
            SetlistList(state, onOpenSetlist, viewModel::togglePast)
        }
    }
}

@Composable
private fun SetlistList(
    state: SetlistsUiState,
    onOpen: (SetlistId) -> Unit,
    onTogglePast: () -> Unit,
) {
    if (state.upcoming.isEmpty() && state.pastCount == 0) {
        EmptyState(
            title = stringResource(Res.string.setlists_empty_title),
            body = stringResource(Res.string.setlists_empty_body),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle(stringResource(Res.string.setlists_upcoming)) }
        if (state.upcoming.isEmpty()) {
            item {
                Text(
                    stringResource(Res.string.setlists_empty_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        cards(state.upcoming, onOpen)
        if (state.pastCount > 0) {
            item {
                TextButton(onClick = onTogglePast) {
                    Text(
                        if (state.showPast) {
                            stringResource(Res.string.setlists_hide_past)
                        } else {
                            stringResource(Res.string.setlists_show_past, state.pastCount.toString())
                        },
                    )
                }
            }
            cards(state.past, onOpen)
        }
    }
}

private fun LazyListScope.cards(
    cards: List<SetlistCard>,
    onOpen: (SetlistId) -> Unit,
) {
    items(cards, key = { it.setlist.id.value }) { card -> SetlistCardItem(card) { onOpen(card.setlist.id) } }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SetlistCardItem(
    card: SetlistCard,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(card.setlist.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (card.hasRoadmap) Tag(stringResource(Res.string.setlist_has_roadmap), tone = TagTone.ACCENT)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BandDot(card.band.color)
                val date = card.setlist.date?.let { dateText(it) } ?: stringResource(Res.string.setlist_no_date)
                Text("${card.band.name} · $date", style = MaterialTheme.typography.bodyMedium)
            }
            val songs = pluralStringResource(Res.plurals.setlist_songs, card.songCount, card.songCount.toString())
            val total = durationText(card.duration.totalWithStopsSeconds)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "$songs · $total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!card.duration.isComplete) {
                    Tag(stringResource(Res.string.setlist_incomplete), tone = TagTone.WARNING)
                }
            }
        }
    }
}
