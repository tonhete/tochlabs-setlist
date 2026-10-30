package com.tochlabs.setlist.feature.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.toColor
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_save
import com.tochlabs.setlist.core.ui.resources.error_required
import com.tochlabs.setlist.core.ui.resources.ic_check
import com.tochlabs.setlist.core.ui.resources.ic_close
import com.tochlabs.setlist.feature.bands.resources.Res
import com.tochlabs.setlist.feature.bands.resources.band_colour
import com.tochlabs.setlist.feature.bands.resources.band_edit
import com.tochlabs.setlist.feature.bands.resources.band_name
import com.tochlabs.setlist.feature.bands.resources.band_new
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Create a band, or edit [bandId]. Calls [onDone] after saving or when the user closes it. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BandFormScreen(
    bandId: BandId?,
    onDone: () -> Unit,
    viewModel: BandFormViewModel = koinViewModel(key = "band-form-${bandId?.value}") { parametersOf(bandId?.value) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) Res.string.band_new else Res.string.band_edit)) },
                navigationIcon = {
                    ActionIcon(
                        CoreRes.drawable.ic_close,
                        stringResource(CoreRes.string.action_cancel),
                        onDone,
                    )
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                    ) { Text(stringResource(CoreRes.string.action_save)) }
                },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(Res.string.band_name)) },
                isError = state.nameError,
                supportingText =
                    if (state.nameError) {
                        (
                            {
                                Text(
                                    stringResource(CoreRes.string.error_required),
                                )
                            }
                        )
                    } else {
                        null
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(Res.string.band_colour), style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                bandPalette.forEach { color ->
                    ColorSwatch(color, selected = color == state.color) { viewModel.onColorChange(color) }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: BandColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(44.dp)
                .background(color.toColor(), CircleShape)
                .then(
                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier,
                ).clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(painterResource(CoreRes.drawable.ic_check), contentDescription = null, tint = Color.White)
    }
}
