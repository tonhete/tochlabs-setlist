package com.tochlabs.setlist.core.data.db

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.scope.Scope

internal const val DATABASE_NAME = "setlist.db"

/** Where the database file lives is platform code: Android needs a Context, iOS the documents folder. */
internal expect fun Scope.databaseBuilder(): RoomDatabase.Builder<SetlistDatabase>

/** Same bundled SQLite on every platform (design.md, decision 3). */
internal fun RoomDatabase.Builder<SetlistDatabase>.buildSetlistDatabase(): SetlistDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
