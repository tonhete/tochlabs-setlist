package com.tochlabs.setlist.di

import android.content.Context
import com.tochlabs.setlist.feature.settings.AppInfo
import org.koin.dsl.module

internal actual val platformModule =
    module {
        single {
            val context = get<Context>()
            AppInfo(
                version =
                    context.packageManager
                        .getPackageInfo(context.packageName, 0)
                        .versionName
                        .orEmpty(),
            )
        }
    }
