package com.tochlabs.setlist.core.data.db

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(
    entities = [
        BandEntity::class,
        SongEntity::class,
        RepertoireEntity::class,
        SetlistEntity::class,
        SetlistItemEntity::class,
        EventEntity::class,
        EventMilestoneEntity::class,
        AppSettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(DateTimeConverters::class)
@ConstructedBy(SetlistDatabaseConstructor::class)
internal abstract class SetlistDatabase : RoomDatabase() {
    abstract fun bandDao(): BandDao

    abstract fun songDao(): SongDao

    abstract fun repertoireDao(): RepertoireDao

    abstract fun setlistDao(): SetlistDao

    abstract fun setlistItemDao(): SetlistItemDao

    abstract fun eventDao(): EventDao

    abstract fun eventMilestoneDao(): EventMilestoneDao

    abstract fun settingsDao(): SettingsDao
}

// Room generates the actual implementations for each platform.
@Suppress("KotlinNoActualForExpect")
internal expect object SetlistDatabaseConstructor : RoomDatabaseConstructor<SetlistDatabase> {
    override fun initialize(): SetlistDatabase
}
