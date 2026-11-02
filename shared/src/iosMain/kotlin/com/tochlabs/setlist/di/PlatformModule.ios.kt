package com.tochlabs.setlist.di

import com.tochlabs.setlist.feature.settings.AppInfo
import org.koin.dsl.module
import platform.Foundation.NSBundle

internal actual val platformModule =
    module {
        single {
            AppInfo(
                version =
                    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "",
            )
        }
    }
