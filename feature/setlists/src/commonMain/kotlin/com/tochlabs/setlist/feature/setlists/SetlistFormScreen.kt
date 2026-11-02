package com.tochlabs.setlist.feature.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BandDot
import com.tochlabs.setlist.core.ui.component.DatePickerDialogFor
import com.tochlabs.setlist.core.ui.format.dateText
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.core.ui.resources.error_required
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.setlists.resources.Res
import com.tochlabs.setlist.feature.setlists.resources.setlist_band
import com.tochlabs.setlist.feature.setlists.resources.setlist_band_required
import com.tochlabs.setlist.feature.setlists.resources.setlist_clear_date
import com.tochlabs.setlist.feature.setlists.resources.setlist_date
import com.tochlabs.setlist.feature.setlists.resources.setlist_edit
import com.tochlabs.setlist.feature.setlists.resources.setlist_name
import com.tochlabs.setlist.feature.setlists.resources.setlist_no_date
import com.tochlabs.setlist.feature.setlists.resources.setlist_pick_date
import com.tochlabs.setlist.feature.setlists.resources.setlists_new
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/**
 * Create a setlist, or edit [setlistId]. [onSaved] receives the saved id; [onClose] leaves without saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistFormScreen(
    setlistId: SetlistId?,
    preselectedBand: BandId?,
    onSaved: (SetlistId) -> Unit,
    onClose: () -> Unit,
    viewModel: SetlistFormViewModel =
        koinViewModel(key = "setlist-form-${setlistId?.value}") {
            parametersOf(setlistId?.value, preselectedBand?.value)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) Res.string.setlists_new else Res.string.setlist_edit)) },
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
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(Res.string.setlist_name)) },
                isError = state.nameError,
                supportingText =
                    if (state.nameError) ({ Text(stringResource(CoreRes.string.error_required)) }) else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            BandPicker(state, viewModel::selectBand)
            DateField(state.date, viewModel::onDateChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BandPicker(
    state: SetlistFormUiState,
    onSelect: (BandId) -> Unit,
) {
    Text(stringResource(Res.string.setlist_band), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.bands.forEach { band ->
            FilterChip(
                selected = band.id == state.bandId,
                onClick = { onSelect(band.id) },
                label = { Text(band.name) },
                leadingIcon = { BandDot(band.color) },
            )
        }
    }
    if (state.bandError) {
        Text(
            stringResource(Res.string.setlist_band_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    Text(stringResource(Res.string.setlist_date), style = MaterialTheme.typography.titleSmall)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { picking = true }) {
            Text(date?.let { dateText(it) } ?: stringResource(Res.string.setlist_pick_date))
        }
        if (date != null) {
            TextButton(onClick = { onDateChange(null) }) { Text(stringResource(Res.string.setlist_clear_date)) }
        } else {
            Text(
                stringResource(Res.string.setlist_no_date),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    if (picking) {
        DatePickerDialogFor(date, onPicked = onDateChange, onDismiss = { picking = false })
    }
}
