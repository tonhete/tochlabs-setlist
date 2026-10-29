package com.tochlabs.setlist.core.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.scope.Scope
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual fun Scope.databaseBuilder(): RoomDatabase.Builder<SetlistDatabase> =
    Room.databaseBuilder("${documentsDirectory()}/$DATABASE_NAME") { SetlistDatabaseConstructor.initialize() }

@OptIn(ExperimentalForeignApi::class)
private fun documentsDirectory(): String {
    val url =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(url?.path) { "Documents directory not found" }
}
