package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tochlabs.setlist.core.domain.setlist.ItemMarkers
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.domain.setlist.effectiveBpm
import com.tochlabs.setlist.core.domain.setlist.effectiveKey
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.DragHandleScope
import com.tochlabs.setlist.core.ui.component.Tag
import com.tochlabs.setlist.core.ui.component.TagTone
import com.tochlabs.setlist.core.ui.format.formatClock
import com.tochlabs.setlist.core.ui.resources.ic_arrow_down
import com.tochlabs.setlist.core.ui.resources.ic_arrow_up
import com.tochlabs.setlist.core.ui.resources.ic_drag_handle
import com.tochlabs.setlist.core.ui.resources.ic_link
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.action_drag
import com.tochlabs.setlist.feature.setlists.resources.action_move_down
import com.tochlabs.setlist.feature.setlists.resources.action_move_up
import com.tochlabs.setlist.feature.setlists.resources.marker_instrument
import com.tochlabs.setlist.feature.setlists.resources.marker_key
import com.tochlabs.setlist.feature.setlists.resources.marker_linked
import com.tochlabs.setlist.feature.setlists.resources.marker_repeated
import com.tochlabs.setlist.feature.setlists.resources.marker_tempo
import com.tochlabs.setlist.feature.setlists.resources.marker_tuning
import com.tochlabs.setlist.feature.setlists.resources.stop_break
import com.tochlabs.setlist.feature.setlists.resources.stop_encore
import com.tochlabs.setlist.feature.setlists.resources.stop_speech
import com.tochlabs.setlist.feature.setlists.resources.stop_tuning
import com.tochlabs.setlist.feature.setlists.resources.tuning_standard
import com.tochlabs.setlist.feature.setlists.resources.value_none
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

internal fun StopType.label(): StringResource =
    when (this) {
        StopType.SPEECH -> Res.string.stop_speech
        StopType.BREAK -> Res.string.stop_break
        StopType.TUNING -> Res.string.stop_tuning
        StopType.ENCORE -> Res.string.stop_encore
    }

/** A song or stop of the setlist. In reorder mode it shows a drag handle and move buttons. */
@Composable
internal fun DragHandleScope.ItemRowView(
    row: ItemRow,
    reorderMode: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Surface(
        tonalElevation = if (isDragging) 6.dp else 0.dp,
        shadowElevation = if (isDragging) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().clickable(enabled = !reorderMode, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (reorderMode) {
                Icon(
                    painterResource(CoreRes.drawable.ic_drag_handle),
                    contentDescription = stringResource(Res.string.action_drag),
                    modifier = Modifier.dragHandle(),
                )
            }
            Text(
                row.songNumber?.toString() ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(24.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (val entry = row.entry) {
                    is ResolvedItem.SongEntry -> SongContent(entry, row.markers)
                    is ResolvedItem.StopEntry -> StopContent(entry)
                }
            }
            if (reorderMode) {
                Row {
                    ActionIcon(
                        CoreRes.drawable.ic_arrow_up,
                        stringResource(Res.string.action_move_up),
                        onClick = onMoveUp,
                        enabled = row.canMoveUp,
                    )
                    ActionIcon(
                        CoreRes.drawable.ic_arrow_down,
                        stringResource(Res.string.action_move_down),
                        onClick = onMoveDown,
                        enabled = row.canMoveDown,
                    )
                }
            } else {
                Trailing(row.entry)
            }
        }
    }
}

@Composable
private fun SongContent(
    entry: ResolvedItem.SongEntry,
    markers: ItemMarkers?,
) {
    Text(entry.song.title, style = MaterialTheme.typography.bodyLarge)
    val details = listOfNotNull(entry.song.artist, entry.item.instrument).joinToString(" · ")
    if (details.isNotEmpty()) {
        Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (markers != null) MarkerTags(markers)
    entry.item.note?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (markers?.isLinkedToNext == true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                painterResource(CoreRes.drawable.ic_link),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                markers.transitionText ?: stringResource(Res.string.marker_linked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** Change markers (specs/setlists, "Change markers"). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkerTags(markers: ItemMarkers) {
    val none = stringResource(Res.string.value_none)
    val standard = stringResource(Res.string.tuning_standard)
    val tags =
        buildList {
            markers.instrumentChangeTo?.let { add(stringResource(Res.string.marker_instrument, it) to TagTone.ACCENT) }
            markers.tuningChange?.let {
                add(
                    stringResource(Res.string.marker_tuning, it.to ?: standard) to TagTone.ACCENT,
                )
            }
            markers.keyChange?.let {
                add(stringResource(Res.string.marker_key, it.to ?: none, it.from ?: none) to TagTone.ACCENT)
            }
            markers.tempoChange?.let {
                val from = it.from?.toString() ?: none
                add(stringResource(Res.string.marker_tempo, it.to?.toString() ?: none, from) to TagTone.ACCENT)
            }
            if (markers.isRepeated) add(stringResource(Res.string.marker_repeated) to TagTone.WARNING)
            if (markers.isLinkedFromPrevious) add(stringResource(Res.string.marker_linked) to TagTone.NEUTRAL)
        }
    if (tags.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tags.forEach { (text, tone) -> Tag(text, tone = tone) }
    }
}

@Composable
private fun StopContent(entry: ResolvedItem.StopEntry) {
    Text(
        stringResource(entry.item.type.label()),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
    entry.item.note?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Trailing(entry: ResolvedItem) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        when (entry) {
            is ResolvedItem.SongEntry -> {
                val musical =
                    listOfNotNull(entry.effectiveKey, entry.effectiveBpm?.let { "$it BPM" }).joinToString(" · ")
                if (musical.isNotEmpty()) Text(musical, style = MaterialTheme.typography.bodySmall)
                entry.song.durationSeconds?.let {
                    Text(formatClock(it.toLong()), style = MaterialTheme.typography.bodyMedium)
                } ?: Text("–", style = MaterialTheme.typography.bodyMedium)
            }
            is ResolvedItem.StopEntry ->
                Text(formatClock(entry.item.durationSeconds.toLong()), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
