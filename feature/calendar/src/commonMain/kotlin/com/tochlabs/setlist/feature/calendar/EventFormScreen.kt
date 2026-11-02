package com.tochlabs.setlist.feature.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.DatePickerDialogFor
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.core.ui.resources.error_required
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.calendar.resources.Res
import com.tochlabs.setlist.feature.calendar.resources.event_address
import com.tochlabs.setlist.feature.calendar.resources.event_band
import com.tochlabs.setlist.feature.calendar.resources.event_band_required
import com.tochlabs.setlist.feature.calendar.resources.event_contact
import com.tochlabs.setlist.feature.calendar.resources.event_date
import com.tochlabs.setlist.feature.calendar.resources.event_date_required
import com.tochlabs.setlist.feature.calendar.resources.event_edit
import com.tochlabs.setlist.feature.calendar.resources.event_new
import com.tochlabs.setlist.feature.calendar.resources.event_parking
import com.tochlabs.setlist.feature.calendar.resources.event_pick_date
import com.tochlabs.setlist.feature.calendar.resources.event_title
import com.tochlabs.setlist.feature.calendar.resources.event_type
import com.tochlabs.setlist.feature.calendar.resources.event_venue
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Create an event (optionally on [date] for [bandId]) or edit [eventId]. [onSaved] receives its id. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventId: EventId?,
    date: LocalDate?,
    bandId: BandId?,
    onSaved: (EventId) -> Unit,
    onClose: () -> Unit,
    viewModel: EventFormViewModel =
        koinViewModel(key = "event-form-${eventId?.value}") {
            parametersOf(eventId?.value, date?.toString(), bandId?.value)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) Res.string.event_new else Res.string.event_edit)) },
                navigationIcon = {
                    ActionIcon(CoreRes.drawable.ic_close, stringResource(CoreRes.string.action_cancel), onClose)
                },
                actions = {
                    TextButton(onClick = viewModel::save) { Text(stringResource(CoreRes.string.action_save)) }
                },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TypeAndBand(state, viewModel)
            DateField(state, viewModel::onDateChange)
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(Res.string.event_title)) },
                isError = state.titleError,
                supportingText =
                    if (state.titleError) ({ Text(stringResource(CoreRes.string.error_required)) }) else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            LogisticsFields(state, viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeAndBand(
    state: EventFormUiState,
    viewModel: EventFormViewModel,
) {
    Text(stringResource(Res.string.event_type), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EventType.entries.forEach { type ->
            FilterChip(
                selected = state.type == type,
                onClick = { viewModel.selectType(type) },
                label = { Text(stringResource(type.label())) },
            )
        }
    }
    Text(stringResource(Res.string.event_band), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.bands.forEach { band ->
            FilterChip(
                selected = band.id == state.bandId,
                onClick = { viewModel.selectBand(band.id) },
                label = { Text(band.name) },
                leadingIcon = { BandDot(band.color) },
            )
        }
    }
    if (state.bandError) ErrorText(stringResource(Res.string.event_band_required))
}

@Composable
private fun DateField(
    state: EventFormUiState,
    onDateChange: (LocalDate) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    Text(stringResource(Res.string.event_date), style = MaterialTheme.typography.titleSmall)
    OutlinedButton(onClick = { picking = true }) {
        Text(state.date?.let { dateText(it) } ?: stringResource(Res.string.event_pick_date))
    }
    if (state.dateError) ErrorText(stringResource(Res.string.event_date_required))
    if (picking) DatePickerDialogFor(state.date, onPicked = onDateChange, onDismiss = { picking = false })
}

@Composable
private fun LogisticsFields(
    state: EventFormUiState,
    viewModel: EventFormViewModel,
) {
    val wide = Modifier.fillMaxWidth()
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    OutlinedTextField(
        value = state.venue,
        onValueChange = viewModel::onVenueChange,
        label = { Text(stringResource(Res.string.event_venue)) },
        singleLine = true,
        keyboardOptions = words,
        modifier = wide,
    )
    OutlinedTextField(
        value = state.address,
        onValueChange = viewModel::onAddressChange,
        label = { Text(stringResource(Res.string.event_address)) },
        keyboardOptions = words,
        modifier = wide,
    )
    OutlinedTextField(
        value = state.parking,
        onValueChange = viewModel::onParkingChange,
        label = { Text(stringResource(Res.string.event_parking)) },
        keyboardOptions = words,
        modifier = wide,
    )
    OutlinedTextField(
        value = state.contact,
        onValueChange = viewModel::onContactChange,
        label = { Text(stringResource(Res.string.event_contact)) },
        keyboardOptions = words,
        modifier = wide,
    )
}

@Composable
internal fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}
