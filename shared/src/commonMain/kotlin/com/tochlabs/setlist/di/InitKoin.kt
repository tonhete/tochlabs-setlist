package com.tochlabs.setlist.di

import com.tochlabs.setlist.core.data.di.dataModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Composition root. Android calls it from Application.onCreate, iOS from the Swift entry point. */
fun initKoin(platform: KoinAppDeclaration = {}) {
    startKoin {
        platform()
        modules(dataModule)
    }
}
