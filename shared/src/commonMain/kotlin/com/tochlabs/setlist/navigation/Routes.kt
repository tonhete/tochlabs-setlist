package com.tochlabs.setlist.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.ic_bands
import com.tochlabs.setlist.core.ui.resources.ic_calendar
import com.tochlabs.setlist.core.ui.resources.ic_setlists
import com.tochlabs.setlist.core.ui.resources.ic_songs
import com.tochlabs.setlist.core.ui.resources.tab_bands
import com.tochlabs.setlist.core.ui.resources.tab_calendar
import com.tochlabs.setlist.core.ui.resources.tab_setlists
import com.tochlabs.setlist.core.ui.resources.tab_songs
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Every destination of the app. Serializable so back stacks survive rotation and process death. */
@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object SetlistsRoute : AppRoute

@Serializable
data object CalendarRoute : AppRoute

@Serializable
data object SongsRoute : AppRoute

@Serializable
data object BandsRoute : AppRoute

@Serializable
data object SettingsRoute : AppRoute

/** Bottom navigation tabs, each with its own back stack (docs/product/screens-and-flows.md). */
enum class Tab(
    val root: AppRoute,
    val label: StringResource,
    val icon: DrawableResource,
) {
    SETLISTS(SetlistsRoute, Res.string.tab_setlists, Res.drawable.ic_setlists),
    CALENDAR(CalendarRoute, Res.string.tab_calendar, Res.drawable.ic_calendar),
    SONGS(SongsRoute, Res.string.tab_songs, Res.drawable.ic_songs),
    BANDS(BandsRoute, Res.string.tab_bands, Res.drawable.ic_bands),
}

/** iOS has no reflection, so the route types are registered explicitly (design.md, decision 5). */
@OptIn(ExperimentalSerializationApi::class)
internal val navigationConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<AppRoute>()
                }
            }
    }
