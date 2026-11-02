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
import com.tochlabs.setlist.core.ui.resources.settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Temporary settings screen; replaced by the settings feature (task group 10).

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
