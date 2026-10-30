package com.tochlabs.setlist.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Bottom navigation with one back stack per tab. Settings open from the app bar of any tab. */
@Composable
fun AppShell() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val stacks = Tab.entries.map { rememberNavBackStack(navigationConfig, it.root) }
    val stack = stacks[selected]

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = index == selected,
                        onClick = {
                            // Tapping the current tab again returns to its root.
                            if (index == selected) stacks[index].popToRoot() else selected = index
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = stack,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            onBack = { stack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    Tab.entries.forEach { tab ->
                        entry(tab.root) {
                            PlaceholderScreen(
                                title = stringResource(tab.label),
                                onOpenSettings = { stack.add(SettingsRoute) },
                            )
                        }
                    }
                    entry<SettingsRoute> { SettingsPlaceholderScreen(onBack = { stack.removeLastOrNull() }) }
                },
        )
    }
}

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeAt(lastIndex)
}
