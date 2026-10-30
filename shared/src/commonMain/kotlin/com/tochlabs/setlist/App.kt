package com.tochlabs.setlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tochlabs.setlist.core.ui.theme.SetlistTheme
import com.tochlabs.setlist.navigation.AppShell

/** Root composable shared by Android and iOS. Koin must be started before it is shown. */
@Composable
@Preview
fun App() {
    SetlistTheme {
        AppShell()
    }
}
