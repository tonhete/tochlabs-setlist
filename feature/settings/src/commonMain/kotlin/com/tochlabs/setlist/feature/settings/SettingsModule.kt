package com.tochlabs.setlist.feature.settings

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Needs an [AppInfo] from the platform (shared module). */
val settingsModule =
    module {
        viewModelOf(::SettingsViewModel)
    }
