package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tochlabs.setlist.core.domain.setlist.DurationSummary
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.format.durationText
import com.tochlabs.setlist.core.ui.format.formatClock
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_remove
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.breakdown_music_only
import com.tochlabs.setlist.feature.setlists.resources.breakdown_with_stops
import com.tochlabs.setlist.feature.setlists.resources.pause_between
import com.tochlabs.setlist.feature.setlists.resources.pause_decrease
import com.tochlabs.setlist.feature.setlists.resources.pause_increase
import com.tochlabs.setlist.feature.setlists.resources.pause_seconds
import com.tochlabs.setlist.feature.setlists.resources.setlist_songs
import com.tochlabs.setlist.feature.setlists.resources.total_exact
import com.tochlabs.setlist.feature.setlists.resources.total_incomplete
import com.tochlabs.setlist.feature.setlists.resources.total_music_only
import com.tochlabs.setlist.feature.setlists.resources.total_with_stops
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Total with stops or music only, exact value, incomplete warning and breakdown (specs/setlist-duration). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TotalCard(
    state: SetlistDetailUiState,
    onModeChange: (TotalMode) -> Unit,
) {
    val duration = state.duration
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TotalMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.totalMode == mode,
                        onClick = { onModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, TotalMode.entries.size),
                    ) { Text(stringResource(mode.label())) }
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(durationText(state.shownTotalSeconds), style = MaterialTheme.typography.displaySmall)
                Text(
                    pluralStringResource(Res.plurals.setlist_songs, state.songCount, state.songCount.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                stringResource(Res.string.total_exact, formatClock(state.shownTotalSeconds)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!duration.isComplete) {
                Text(
                    pluralStringResource(
                        Res.plurals.total_incomplete,
                        duration.songsWithoutDuration,
                        duration.songsWithoutDuration.toString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                breakdown(state.totalMode, duration, state.setlist?.pauseSeconds ?: 0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun TotalMode.label() =
    when (this) {
        TotalMode.WITH_STOPS -> Res.string.total_with_stops
        TotalMode.MUSIC_ONLY -> Res.string.total_music_only
    }

@Composable
private fun breakdown(
    mode: TotalMode,
    duration: DurationSummary,
    pauseSeconds: Int,
): String =
    when (mode) {
        TotalMode.WITH_STOPS ->
            stringResource(
                Res.string.breakdown_with_stops,
                formatClock(duration.musicSeconds),
                formatClock(duration.stopsSeconds),
                duration.pauseCount.toString(),
                pauseSeconds.toString(),
                duration.linkedPairs.toString(),
            )
        TotalMode.MUSIC_ONLY ->
            stringResource(Res.string.breakdown_music_only, formatClock(duration.leftOutOfMusicOnlySeconds))
    }

/** Stepper in 5 second steps between 0 and 120; the buttons do nothing at the limits. */
@Composable
internal fun PauseStepper(
    state: SetlistDetailUiState,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(Res.string.pause_between),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        ActionIcon(
            CoreRes.drawable.ic_remove,
            stringResource(Res.string.pause_decrease),
            onClick = onDecrease,
            enabled = state.canDecreasePause,
        )
        Text(
            stringResource(Res.string.pause_seconds, (state.setlist?.pauseSeconds ?: 0).toString()),
            style = MaterialTheme.typography.titleMedium,
        )
        ActionIcon(
            CoreRes.drawable.ic_add,
            stringResource(Res.string.pause_increase),
            onClick = onIncrease,
            enabled = state.canIncreasePause,
        )
    }
}
