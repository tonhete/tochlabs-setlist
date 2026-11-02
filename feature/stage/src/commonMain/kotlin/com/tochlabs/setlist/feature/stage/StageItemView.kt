package com.tochlabs.setlist.feature.stage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tochlabs.setlist.core.domain.setlist.ItemMarkers
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.ui.format.formatClock
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.resources.ic_link
import com.tochlabs.setlist.core.ui.resources.marker_instrument
import com.tochlabs.setlist.core.ui.resources.marker_key
import com.tochlabs.setlist.core.ui.resources.marker_linked
import com.tochlabs.setlist.core.ui.resources.marker_repeated
import com.tochlabs.setlist.core.ui.resources.marker_tempo
import com.tochlabs.setlist.core.ui.resources.marker_tuning
import com.tochlabs.setlist.core.ui.resources.tuning_standard
import com.tochlabs.setlist.core.ui.resources.value_none
import com.tochlabs.setlist.feature.stage.resources.Res
import com.tochlabs.setlist.feature.stage.resources.stage_no_duration
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

@Composable
internal fun StageItemView(item: StageItem) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val entry = item.entry) {
            is ResolvedItem.SongEntry -> SongView(item, entry)
            is ResolvedItem.StopEntry -> StopView(item, entry)
        }
        item.note?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun SongView(
    item: StageItem,
    entry: ResolvedItem.SongEntry,
) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            item.songNumber.toString(),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(entry.song.title, fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)
    }
    entry.song.artist?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BigValue(item.key ?: "–")
        BigValue(item.bpm?.let { "$it BPM" } ?: "– BPM")
        BigValue(item.durationSeconds?.let { formatClock(it.toLong()) } ?: stringResource(Res.string.stage_no_duration))
    }
    item.markers?.let { StageMarkers(it) }
}

@Composable
private fun StopView(
    item: StageItem,
    entry: ResolvedItem.StopEntry,
) {
    Text(
        stringResource(entry.item.type.label()),
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
    )
    BigValue(formatClock((item.durationSeconds ?: 0).toLong()))
}

@Composable
private fun BigValue(text: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

/** Change markers and the link to the next song, large enough to read from a distance. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StageMarkers(markers: ItemMarkers) {
    val tags = markerTexts(markers)
    if (tags.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
    if (markers.isLinkedToNext) LinkLine(markers.transitionText)
}

@Composable
private fun markerTexts(markers: ItemMarkers): List<String> {
    val none = stringResource(CoreRes.string.value_none)
    val standard = stringResource(CoreRes.string.tuning_standard)
    return buildList {
        markers.instrumentChangeTo?.let { add(stringResource(CoreRes.string.marker_instrument, it)) }
        markers.tuningChange?.let { add(stringResource(CoreRes.string.marker_tuning, it.to ?: standard)) }
        markers.keyChange?.let { add(stringResource(CoreRes.string.marker_key, it.to ?: none, it.from ?: none)) }
        markers.tempoChange?.let {
            add(stringResource(CoreRes.string.marker_tempo, it.to?.toString() ?: none, it.from?.toString() ?: none))
        }
        if (markers.isRepeated) add(stringResource(CoreRes.string.marker_repeated))
    }
}

@Composable
private fun LinkLine(transitionText: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            painterResource(CoreRes.drawable.ic_link),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Text(
            transitionText ?: stringResource(CoreRes.string.marker_linked),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
