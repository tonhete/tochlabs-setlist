package com.tochlabs.setlist

import android.app.Application
import com.tochlabs.setlist.di.initKoin
import org.koin.android.ext.koin.androidContext

class SetlistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@SetlistApplication) }
    }
}
