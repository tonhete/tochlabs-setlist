package com.tochlabs.setlist.di

/** Called from the Swift entry point (iOSApp.init). Swift cannot use the default argument of [initKoin]. */
fun startKoinForIos() = initKoin()
