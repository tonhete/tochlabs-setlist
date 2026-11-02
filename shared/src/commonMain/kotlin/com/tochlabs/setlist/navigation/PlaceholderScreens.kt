package com.tochlabs.setlist.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tochlabs.setlist.core.ui.component.EmptyState
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.back
import com.tochlabs.setlist.core.ui.resources.coming_soon
import com.tochlabs.setlist.core.ui.resources.ic_back
import com.tochlabs.setlist.core.ui.resources.ic_settings
import com.tochlabs.setlist.core.ui.resources.settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Temporary tab and settings screens; the feature modules replace them (task groups 8 and 10).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceholderScreen(
    tab: Tab,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(tab.label)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(Res.drawable.ic_settings), stringResource(Res.string.settings))
                    }
                },
            )
        },
    ) { padding ->
        EmptyState(title = stringResource(Res.string.coming_soon), modifier = Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsPlaceholderScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_back), stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { padding ->
        EmptyState(title = stringResource(Res.string.coming_soon), modifier = Modifier.padding(padding))
    }
}
