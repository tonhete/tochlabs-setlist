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
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.feature.bands.BandFormScreen
import com.tochlabs.setlist.feature.bands.BandHistoryScreen
import com.tochlabs.setlist.feature.bands.BandsScreen
import com.tochlabs.setlist.feature.setlists.SetlistDetailScreen
import com.tochlabs.setlist.feature.setlists.SetlistFormScreen
import com.tochlabs.setlist.feature.setlists.SetlistsScreen
import com.tochlabs.setlist.feature.songs.SongFormScreen
import com.tochlabs.setlist.feature.songs.SongsScreen
import com.tochlabs.setlist.feature.stage.StageScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    Scaffold(
        bottomBar = {
            if (stack.lastOrNull() !is StageRoute) AppNavigationBar(selected, stacks) { selected = it }
        },
    ) { padding ->
        // One NavDisplay per tab: switching tabs must not look like popping the other tab's screens.
        key(selected) {
            NavDisplay(
                backStack = stack,
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
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

private fun appEntries(stack: NavBackStack<NavKey>) =
    entryProvider<NavKey> {
        val back: () -> Unit = { stack.removeLastOrNull() }
        val openSettings: () -> Unit = { stack.add(SettingsRoute) }

        entry<SetlistsRoute> {
            SetlistsScreen(
                onOpenSettings = openSettings,
                onAddSetlist = { band -> stack.add(SetlistFormRoute(bandId = band?.value)) },
                onOpenSetlist = { stack.add(SetlistDetailRoute(it.value)) },
            )
        }
        entry<SetlistFormRoute> { route ->
            SetlistFormScreen(
                setlistId = route.setlistId?.let(::SetlistId),
                preselectedBand = route.bandId?.let(::BandId),
                onSaved = { id ->
                    back()
                    // A new setlist opens its detail, ready to add songs.
                    if (route.setlistId == null) stack.add(SetlistDetailRoute(id.value))
                },
                onClose = back,
            )
        }
        entry<SetlistDetailRoute> { route ->
            SetlistDetailScreen(
                setlistId = SetlistId(route.setlistId),
                onBack = back,
                onEdit = { stack.add(SetlistFormRoute(setlistId = it.value)) },
                onOpenStage = { stack.add(StageRoute(it.value)) },
            )
        }
        entry<StageRoute> { route -> StageScreen(SetlistId(route.setlistId), onExit = back) }
        entry<CalendarRoute> { PlaceholderScreen(Tab.CALENDAR, openSettings) }
        entry<SettingsRoute> { SettingsPlaceholderScreen(onBack = back) }

        entry<SongsRoute> {
            SongsScreen(
                onOpenSettings = openSettings,
                onAddSong = { stack.add(SongFormRoute()) },
                onEditSong = { stack.add(SongFormRoute(it.value)) },
            )
        }
        entry<SongFormRoute> { route -> SongFormScreen(route.songId?.let(::SongId), onDone = back) }

        entry<BandsRoute> {
            BandsScreen(
                onOpenSettings = openSettings,
                onOpenHistory = { stack.add(BandHistoryRoute) },
                onAddBand = { stack.add(BandFormRoute()) },
                onEditBand = { stack.add(BandFormRoute(it.value)) },
            )
        }
        entry<BandHistoryRoute> { BandHistoryScreen(onBack = back) }
        entry<BandFormRoute> { route -> BandFormScreen(route.bandId?.let(::BandId), onDone = back) }
    }

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeAt(lastIndex)
}
