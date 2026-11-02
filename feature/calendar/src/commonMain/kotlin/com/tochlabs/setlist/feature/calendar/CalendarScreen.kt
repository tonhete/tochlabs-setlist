package com.tochlabs.setlist.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.BandFilterChips
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.component.SettingsAction
import com.tochlabs.setlist.core.ui.component.toColor
import com.tochlabs.setlist.core.ui.format.currentUiLanguage
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.format.formatMonth
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.format.weekdayInitials
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_chevron_left
import com.tochlabs.setlist.core.ui.resources.ic_chevron_right
import com.tochlabs.setlist.core.ui.resources.tab_calendar
import com.tochlabs.setlist.feature.calendar.resources.Res
import com.tochlabs.setlist.feature.calendar.resources.agenda_day
import com.tochlabs.setlist.feature.calendar.resources.agenda_empty
import com.tochlabs.setlist.feature.calendar.resources.agenda_month
import com.tochlabs.setlist.feature.calendar.resources.agenda_show_month
import com.tochlabs.setlist.feature.calendar.resources.calendar_next_month
import com.tochlabs.setlist.feature.calendar.resources.calendar_no_bands_body
import com.tochlabs.setlist.feature.calendar.resources.calendar_no_bands_title
import com.tochlabs.setlist.feature.calendar.resources.calendar_previous_month
import com.tochlabs.setlist.feature.calendar.resources.calendar_today
import com.tochlabs.setlist.feature.calendar.resources.event_new
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Calendar tab root: band filter, monthly grid and agenda (specs/event-calendar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onOpenSettings: () -> Unit,
    /** New event, prefilled with the selected day and the filtered band. */
    onAddEvent: (LocalDate?, BandId?) -> Unit,
    onOpenEvent: (EventId) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.tab_calendar)) },
                actions = {
                    TextButton(onClick = viewModel::showToday) { Text(stringResource(Res.string.calendar_today)) }
                    SettingsAction(onOpenSettings)
                },
            )
        },
        floatingActionButton = {
            if (!state.loading && !state.noBands) {
                FloatingActionButton(onClick = { onAddEvent(state.selectedDay, state.selectedBand) }) {
                    Icon(painterResource(CoreRes.drawable.ic_add), stringResource(Res.string.event_new))
                }
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        if (state.noBands) {
            EmptyState(
                title = stringResource(Res.string.calendar_no_bands_title),
                body = stringResource(Res.string.calendar_no_bands_body),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            item { BandFilterChips(state.bands, state.selectedBand, viewModel::selectBand) }
            item {
                MonthHeader(state.month, viewModel::previousMonth, viewModel::nextMonth)
                MonthGrid(state, viewModel::selectDay)
            }
            item { AgendaHeader(state, onShowMonth = { state.selectedDay?.let(viewModel::selectDay) }) }
            if (state.agenda.isEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.agenda_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            items(state.agenda, key = { it.event.id.value }) { entry ->
                AgendaCard(entry, showDate = state.selectedDay == null) { onOpenEvent(entry.event.id) }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        ActionIcon(CoreRes.drawable.ic_chevron_left, stringResource(Res.string.calendar_previous_month), onPrevious)
        Text(
            formatMonth(month, currentUiLanguage()).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        ActionIcon(CoreRes.drawable.ic_chevron_right, stringResource(Res.string.calendar_next_month), onNext)
    }
}

@Composable
private fun MonthGrid(
    state: CalendarUiState,
    onSelect: (LocalDate) -> Unit,
) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row {
            weekdayInitials(currentUiLanguage()).forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        state.weeks.forEach { week ->
            Row { week.forEach { day -> DayCell(day, Modifier.weight(1f)) { onSelect(day.date) } } }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val label = dateText(day.date)
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .then(if (day.isSelected) Modifier.background(colors.primary, CircleShape) else Modifier)
                .then(
                    if (day.isToday &&
                        !day.isSelected
                    ) {
                        Modifier.border(1.dp, colors.primary, CircleShape)
                    } else {
                        Modifier
                    },
                ).clickable(onClick = onClick)
                .semantics {
                    contentDescription = label
                    selected = day.isSelected
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            day.date.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            color =
                when {
                    day.isSelected -> colors.onPrimary
                    day.inMonth -> colors.onSurface
                    else -> colors.outline
                },
        )
        if (day.bandColors.isNotEmpty()) {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                day.bandColors.take(MAX_DOTS).forEach {
                    Box(Modifier.size(5.dp).background(it.toColor(), CircleShape))
                }
            }
        }
    }
}

@Composable
private fun AgendaHeader(
    state: CalendarUiState,
    onShowMonth: () -> Unit,
) {
    val day = state.selectedDay
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (day == null) {
                stringResource(Res.string.agenda_month, formatMonth(state.month, currentUiLanguage()))
            } else {
                stringResource(Res.string.agenda_day, dateText(day))
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (day != null) {
            TextButton(onClick = onShowMonth) { Text(stringResource(Res.string.agenda_show_month)) }
        }
    }
}

@Composable
private fun AgendaCard(
    entry: AgendaEntry,
    showDate: Boolean,
    onClick: () -> Unit,
) {
    val event = entry.event
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BandDot(entry.band.color)
                val parts =
                    listOfNotNull(
                        entry.band.name,
                        stringResource(event.type.label()),
                        if (showDate) dateText(event.date) else null,
                        event.venue,
                    )
                Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private const val MAX_DOTS = 3
