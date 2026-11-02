package com.tochlabs.setlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.ui.format.AppLanguageEnvironment
import com.tochlabs.setlist.core.ui.theme.SetlistTheme
import com.tochlabs.setlist.navigation.AppShell
import org.koin.compose.koinInject

/** Root composable shared by Android and iOS. Koin must be started before it is shown. */
@Composable
fun App() {
    val settingsRepository = koinInject<SettingsRepository>()
    val settings by remember(settingsRepository) { settingsRepository.observe() }.collectAsState(initial = null)
    // Wait for the stored language (a few milliseconds) so the first screen is not shown in another one.
    val current = settings ?: return
    AppLanguageEnvironment(current.language) {
        SetlistTheme {
            AppShell()
        }
    }
}
