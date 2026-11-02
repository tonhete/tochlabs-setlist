package com.tochlabs.setlist.core.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.back
import com.tochlabs.setlist.core.ui.resources.ic_back
import com.tochlabs.setlist.core.ui.resources.ic_settings
import com.tochlabs.setlist.core.ui.resources.settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Icon button with an accessible label. */
@Composable
fun ActionIcon(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
) = ActionIcon(icon, label, enabled = true, onClick = onClick)

/** Icon button with an accessible label that can be disabled, for example a stepper at its limit. */
@Composable
fun ActionIcon(
    icon: DrawableResource,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(painterResource(icon), contentDescription = label)
    }
}

/** App bar action that opens Settings; shown on the root screen of every tab. */
@Composable
fun SettingsAction(onClick: () -> Unit) =
    ActionIcon(Res.drawable.ic_settings, stringResource(Res.string.settings), onClick)

/** App bar navigation icon for screens below a tab root. */
@Composable
fun BackButton(onClick: () -> Unit) = ActionIcon(Res.drawable.ic_back, stringResource(Res.string.back), onClick)

/**
 * Shows a one-off message from the UI state in a snackbar and then reports it as shown,
 * so the ViewModel can clear it (design.md decision 4: effects are state the UI consumes).
 */
@Composable
fun MessageEffect(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            currentOnShown()
        }
    }
}
