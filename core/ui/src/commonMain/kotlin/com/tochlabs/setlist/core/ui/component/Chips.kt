package com.tochlabs.setlist.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.filter_all_bands
import org.jetbrains.compose.resources.stringResource

fun BandColor.toColor(): Color = Color(argb)

/** Small coloured circle that identifies a band. */
@Composable
fun BandDot(
    color: BandColor,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
) {
    Box(modifier.size(size).background(color.toColor(), CircleShape))
}

/** Band filter: "All" plus one chip per active band (specs/bands). Null means all bands. */
@Composable
fun BandFilterChips(
    bands: List<Band>,
    selected: BandId?,
    onSelect: (BandId?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(Res.string.filter_all_bands)) },
        )
        bands.forEach { band ->
            FilterChip(
                selected = selected == band.id,
                onClick = { onSelect(band.id) },
                label = { Text(band.name) },
                leadingIcon = { BandDot(band.color) },
            )
        }
    }
}

enum class TagTone { NEUTRAL, ACCENT, WARNING }

/** Compact label for change markers and badges such as "repeated" or "has roadmap". */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: TagTone = TagTone.NEUTRAL,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) =
        when (tone) {
            TagTone.NEUTRAL -> colors.surfaceVariant to colors.onSurfaceVariant
            TagTone.ACCENT -> colors.secondaryContainer to colors.onSecondaryContainer
            TagTone.WARNING -> colors.errorContainer to colors.onErrorContainer
        }
    Surface(modifier = modifier, shape = RoundedCornerShape(6.dp), color = container, contentColor = content) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
