package com.tochlabs.setlist.feature.stage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.stage.resources.Res
import com.tochlabs.setlist.feature.stage.resources.stage_exit
import com.tochlabs.setlist.feature.stage.resources.stage_last
import com.tochlabs.setlist.feature.stage.resources.stage_next
import com.tochlabs.setlist.feature.stage.resources.stage_next_preview
import com.tochlabs.setlist.feature.stage.resources.stage_position
import com.tochlabs.setlist.feature.stage.resources.stage_previous
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** One item at a time, large, with swipe and buttons (specs/stage-mode). [onExit] returns to the setlist. */
@Composable
fun StageScreen(
    setlistId: SetlistId,
    onExit: () -> Unit,
    viewModel: StageViewModel = koinViewModel(key = "stage-${setlistId.value}") { parametersOf(setlistId.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // The setlist view refuses an empty setlist; this covers items removed meanwhile.
    LaunchedEffect(state.empty) { if (state.empty) onExit() }
    if (state.loading || state.empty) return

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            TopLine(state, onExit)
            ItemPager(state, viewModel::goTo, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopLine(
    state: StageUiState,
    onExit: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        ActionIcon(CoreRes.drawable.ic_close, stringResource(Res.string.stage_exit), onExit)
        Text(
            state.setlistName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(Res.string.stage_position, state.position.toString(), state.total.toString()),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

/** Swipe between items; the buttons animate the same pager, and the settled page is the current item. */
@Composable
private fun ItemPager(
    state: StageUiState,
    onSettled: (Int) -> Unit,
    modifier: Modifier,
) {
    val pager = rememberPagerState(initialPage = state.index) { state.total }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager.settledPage) { onSettled(pager.settledPage) }

    Column(modifier) {
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { page ->
            state.items.getOrNull(page)?.let { StageItemView(it) }
        }
        NextPreview(state.nextItem)
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = { scope.launch { pager.animateScrollToPage(state.index - 1) } },
                enabled = state.canGoBack,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(Res.string.stage_previous), style = MaterialTheme.typography.titleMedium) }
            FilledTonalButton(
                onClick = { scope.launch { pager.animateScrollToPage(state.index + 1) } },
                enabled = state.canGoForward,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(Res.string.stage_next), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun NextPreview(next: StageItem?) {
    val text =
        if (next == null) {
            stringResource(Res.string.stage_last)
        } else {
            stringResource(Res.string.stage_next_preview, next.title())
        }
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@Composable
private fun StageItem.title(): String =
    when (val entry = entry) {
        is ResolvedItem.SongEntry -> entry.song.title
        is ResolvedItem.StopEntry -> stringResource(entry.item.type.label())
    }
