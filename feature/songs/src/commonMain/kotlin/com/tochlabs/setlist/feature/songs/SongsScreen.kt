package com.tochlabs.setlist.feature.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.BandFilterChips
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.SettingsAction
import com.tochlabs.setlist.core.ui.component.Tag
import com.tochlabs.setlist.core.ui.component.TagTone
import com.tochlabs.setlist.core.ui.format.formatClock
import com.tochlabs.setlist.core.ui.resources.action_clear
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.core.ui.resources.ic_search
import com.tochlabs.setlist.core.ui.resources.tab_songs
import com.tochlabs.setlist.feature.songs.resources.Res
import com.tochlabs.setlist.feature.songs.resources.song_new
import com.tochlabs.setlist.feature.songs.resources.song_no_duration
import com.tochlabs.setlist.feature.songs.resources.songs_band_empty
import com.tochlabs.setlist.feature.songs.resources.songs_empty_body
import com.tochlabs.setlist.feature.songs.resources.songs_empty_title
import com.tochlabs.setlist.feature.songs.resources.songs_no_results
import com.tochlabs.setlist.feature.songs.resources.songs_originals_note
import com.tochlabs.setlist.feature.songs.resources.songs_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Songs tab root: the catalogue with band filter and search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onOpenSettings: () -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (SongId) -> Unit,
    viewModel: SongsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.tab_songs)) },
                actions = { SettingsAction(onOpenSettings) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSong) {
                Icon(painterResource(CoreRes.drawable.ic_add), stringResource(Res.string.song_new))
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        if (state.catalogueEmpty) {
            EmptyState(
                title = stringResource(Res.string.songs_empty_title),
                body = stringResource(Res.string.songs_empty_body),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchField(state.query, viewModel::onQueryChange)
            if (state.bands.isNotEmpty()) {
                BandFilterChips(state.bands, state.selectedBand, viewModel::selectBand)
            }
            if (state.songs.isEmpty()) {
                val empty = if (state.query.isBlank()) Res.string.songs_band_empty else Res.string.songs_no_results
                EmptyState(title = stringResource(empty))
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        Text(
                            stringResource(Res.string.songs_originals_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(state.songs, key = { it.song.id.value }) { row ->
                        SongRowItem(row, onClick = { onEditSong(row.song.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.songs_search)) },
        leadingIcon = { Icon(painterResource(CoreRes.drawable.ic_search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ActionIcon(CoreRes.drawable.ic_close, stringResource(CoreRes.string.action_clear)) { onQueryChange("") }
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SongRowItem(
    row: SongRow,
    onClick: () -> Unit,
) {
    val song = row.song
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(song.title) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val details = listOfNotNull(song.artist, song.originalTuning).joinToString(" · ")
                if (details.isNotEmpty()) Text(details)
                if (row.bands.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.bands.forEach { BandDot(it.color) }
                    }
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val musical = listOfNotNull(song.originalKey, song.originalBpm?.let { "$it BPM" }).joinToString(" · ")
                if (musical.isNotEmpty()) Text(musical, style = MaterialTheme.typography.bodySmall)
                val duration = song.durationSeconds
                if (duration == null) {
                    Tag(stringResource(Res.string.song_no_duration), tone = TagTone.WARNING)
                } else {
                    Text(formatClock(duration.toLong()), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )
}
