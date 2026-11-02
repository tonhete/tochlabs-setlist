package com.tochlabs.setlist.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tochlabs.setlist.feature.share.IncomingSetlistFiles
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Bottom navigation with one back stack per tab. Settings open from the app bar of any tab. */
@Composable
fun AppShell() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val stacks = Tab.entries.map { rememberNavBackStack(navigationConfig, it.root) }
    val stack = stacks[selected]
    // Decorators give each screen its own saved state and ViewModels, cleared when the screen is
    // popped. They are created here, once per tab, so they survive while their tab is hidden:
    // created inside the tab's NavDisplay they would be disposed with it and unsaved input lost.
    val decorators =
        Tab.entries.map {
            listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator<NavKey>(),
            )
        }

    // A `.setlist` file opened from another app or picked to import: show the import screen.
    val inbox = koinInject<IncomingSetlistFiles>()
    val incoming by inbox.hasPending.collectAsState()
    LaunchedEffect(incoming) {
        if (incoming) {
            selected = Tab.SETLISTS.ordinal
            val setlists = stacks[Tab.SETLISTS.ordinal]
            if (setlists.lastOrNull() != ImportRoute) setlists.add(ImportRoute)
        }
    }

    Scaffold(
        bottomBar = {
            if (stack.lastOrNull() !is StageRoute) AppNavigationBar(selected, stacks) { selected = it }
        },
    ) { padding ->
        // One NavDisplay per tab: switching tabs must not look like popping the other tab's screens.
        key(selected) {
            NavDisplay(
                backStack = stack,
                // imePadding keeps forms and their app bar above the keyboard (the window is not panned).
                modifier = Modifier.padding(padding).consumeWindowInsets(padding).imePadding(),
                onBack = { stack.removeLastOrNull() },
                entryDecorators = decorators[selected],
                entryProvider = appEntries(stack),
            )
        }
    }
}

@Composable
private fun AppNavigationBar(
    selected: Int,
    stacks: List<NavBackStack<NavKey>>,
    onSelect: (Int) -> Unit,
) {
    NavigationBar {
        Tab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selected,
                onClick = {
                    // Tapping the current tab again returns to its root.
                    if (index == selected) stacks[index].popToRoot() else onSelect(index)
                },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeAt(lastIndex)
}
