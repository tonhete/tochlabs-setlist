package com.tochlabs.setlist.core.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun Scope.databaseBuilder(): RoomDatabase.Builder<SetlistDatabase> {
    val context = androidContext()
    val file = context.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder(context, file.absolutePath) { SetlistDatabaseConstructor.initialize() }
}
