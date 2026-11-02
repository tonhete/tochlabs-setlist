package com.tochlabs.setlist.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.ui.component.ActionIcon
import com.tochlabs.setlist.core.ui.component.BackButton
import com.tochlabs.setlist.core.ui.resources.ic_add
import com.tochlabs.setlist.core.ui.resources.ic_remove
import com.tochlabs.setlist.core.ui.resources.settings
import com.tochlabs.setlist.feature.settings.resources.Res
import com.tochlabs.setlist.feature.settings.resources.about_licence
import com.tochlabs.setlist.feature.settings.resources.about_source
import com.tochlabs.setlist.feature.settings.resources.about_tagline
import com.tochlabs.setlist.feature.settings.resources.about_version
import com.tochlabs.setlist.feature.settings.resources.language_en
import com.tochlabs.setlist.feature.settings.resources.language_es
import com.tochlabs.setlist.feature.settings.resources.language_system
import com.tochlabs.setlist.feature.settings.resources.pause_decrease
import com.tochlabs.setlist.feature.settings.resources.pause_increase
import com.tochlabs.setlist.feature.settings.resources.pause_seconds
import com.tochlabs.setlist.feature.settings.resources.settings_about
import com.tochlabs.setlist.feature.settings.resources.settings_default_pause
import com.tochlabs.setlist.feature.settings.resources.settings_default_pause_note
import com.tochlabs.setlist.feature.settings.resources.settings_language
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/** Language, default pause and about (specs/app-settings). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.settings)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LanguageSection(state.language, viewModel::selectLanguage)
            PauseSection(state, viewModel)
            AboutSection(state.about)
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private val LANGUAGE_OPTIONS: List<Pair<AppLanguage, StringResource>> =
    listOf(
        AppLanguage.SYSTEM to Res.string.language_system,
        AppLanguage.ES to Res.string.language_es,
        AppLanguage.EN to Res.string.language_en,
    )

@Composable
private fun LanguageSection(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Section(stringResource(Res.string.settings_language)) {
        Column(Modifier.selectableGroup()) {
            LANGUAGE_OPTIONS.forEach { (language, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = language == selected, role = Role.RadioButton) { onSelect(language) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = language == selected, onClick = null)
                    Text(stringResource(label), modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun PauseSection(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
) {
    Section(stringResource(Res.string.settings_default_pause)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionIcon(
                CoreRes.drawable.ic_remove,
                stringResource(Res.string.pause_decrease),
                enabled = state.canDecreasePause,
                onClick = viewModel::decreaseDefaultPause,
            )
            Text(
                stringResource(Res.string.pause_seconds, state.defaultPauseSeconds.toString()),
                style = MaterialTheme.typography.titleLarge,
            )
            ActionIcon(
                CoreRes.drawable.ic_add,
                stringResource(Res.string.pause_increase),
                enabled = state.canIncreasePause,
                onClick = viewModel::increaseDefaultPause,
            )
        }
        Text(
            stringResource(Res.string.settings_default_pause_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutSection(about: About) {
    val uriHandler = LocalUriHandler.current
    Section(stringResource(Res.string.settings_about)) {
        Text("TochLabs Setlist", style = MaterialTheme.typography.titleSmall)
        Text(stringResource(Res.string.about_tagline), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(Res.string.about_version, about.version), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(Res.string.about_licence, about.licence), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { uriHandler.openUri(about.repositoryUrl) }) {
            Text(stringResource(Res.string.about_source))
        }
        Text(
            about.repositoryUrl,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
