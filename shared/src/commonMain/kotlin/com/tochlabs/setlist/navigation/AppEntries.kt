package com.tochlabs.setlist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.feature.bands.BandFormScreen
import com.tochlabs.setlist.feature.bands.BandHistoryScreen
import com.tochlabs.setlist.feature.bands.BandsScreen
import com.tochlabs.setlist.feature.calendar.CalendarScreen
import com.tochlabs.setlist.feature.calendar.EventDetailScreen
import com.tochlabs.setlist.feature.calendar.EventFormScreen
import com.tochlabs.setlist.feature.setlists.SetlistDetailScreen
import com.tochlabs.setlist.feature.setlists.SetlistFormScreen
import com.tochlabs.setlist.feature.setlists.SetlistsScreen
import com.tochlabs.setlist.feature.songs.SongFormScreen
import com.tochlabs.setlist.feature.songs.SongsScreen
import com.tochlabs.setlist.feature.stage.StageScreen
import kotlinx.datetime.LocalDate

/** Screens of one tab's back stack. Any route can be pushed on any tab, for example a setlist from an event. */
internal fun appEntries(stack: NavBackStack<NavKey>) =
    entryProvider<NavKey> {
        val back: () -> Unit = { stack.removeLastOrNull() }
        val openSettings: () -> Unit = { stack.add(SettingsRoute) }
        setlistEntries(stack, back, openSettings)
        calendarEntries(stack, back, openSettings)
        songEntries(stack, back, openSettings)
        bandEntries(stack, back, openSettings)
        entry<SettingsRoute> { SettingsPlaceholderScreen(onBack = back) }
    }

/** Setlists, their form and detail, and stage mode. */
private fun EntryProviderScope<NavKey>.setlistEntries(
    stack: NavBackStack<NavKey>,
    back: () -> Unit,
    openSettings: () -> Unit,
) {
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
}

/** Calendar, event form and event sheet. */
private fun EntryProviderScope<NavKey>.calendarEntries(
    stack: NavBackStack<NavKey>,
    back: () -> Unit,
    openSettings: () -> Unit,
) {
    entry<CalendarRoute> {
        CalendarScreen(
            onOpenSettings = openSettings,
            onAddEvent = { date, band -> stack.add(EventFormRoute(date = date?.toString(), bandId = band?.value)) },
            onOpenEvent = { stack.add(EventDetailRoute(it.value)) },
        )
    }
    entry<EventFormRoute> { route ->
        EventFormScreen(
            eventId = route.eventId?.let(::EventId),
            date = route.date?.let(LocalDate::parse),
            bandId = route.bandId?.let(::BandId),
            onSaved = { id ->
                back()
                // A new event opens its sheet, ready for the roadmap and the setlist.
                if (route.eventId == null) stack.add(EventDetailRoute(id.value))
            },
            onClose = back,
        )
    }
    entry<EventDetailRoute> { route ->
        EventDetailScreen(
            eventId = EventId(route.eventId),
            onBack = back,
            onEdit = { stack.add(EventFormRoute(eventId = it.value)) },
            onOpenSetlist = { stack.add(SetlistDetailRoute(it.value)) },
        )
    }
}

/** Song catalogue and song form. */
private fun EntryProviderScope<NavKey>.songEntries(
    stack: NavBackStack<NavKey>,
    back: () -> Unit,
    openSettings: () -> Unit,
) {
    entry<SongsRoute> {
        SongsScreen(
            onOpenSettings = openSettings,
            onAddSong = { stack.add(SongFormRoute()) },
            onEditSong = { stack.add(SongFormRoute(it.value)) },
        )
    }
    entry<SongFormRoute> { route -> SongFormScreen(route.songId?.let(::SongId), onDone = back) }
}

/** Bands, history and band form. */
private fun EntryProviderScope<NavKey>.bandEntries(
    stack: NavBackStack<NavKey>,
    back: () -> Unit,
    openSettings: () -> Unit,
) {
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
