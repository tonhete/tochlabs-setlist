package com.tochlabs.setlist.core.data.di

import com.tochlabs.setlist.core.data.SystemClock
import com.tochlabs.setlist.core.data.UuidIdGenerator
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.buildSetlistDatabase
import com.tochlabs.setlist.core.data.db.databaseBuilder
import com.tochlabs.setlist.core.data.repository.RoomBandRepository
import com.tochlabs.setlist.core.data.repository.RoomEventRepository
import com.tochlabs.setlist.core.data.repository.RoomSetlistRepository
import com.tochlabs.setlist.core.data.repository.RoomSetlistTransferRepository
import com.tochlabs.setlist.core.data.repository.RoomSettingsRepository
import com.tochlabs.setlist.core.data.repository.RoomSongRepository
import com.tochlabs.setlist.core.data.transfer.JsonSetlistFileCodec
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.domain.repository.SongRepository
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.domain.transfer.SetlistFileCodec
import com.tochlabs.setlist.core.domain.transfer.SetlistTransferRepository
import org.koin.dsl.module

/** Database, clock, id generator and repositories. Features only see the core:domain interfaces. */
val dataModule =
    module {
        single<SetlistDatabase> { databaseBuilder().buildSetlistDatabase() }
        single<Clock> { SystemClock }
        single<IdGenerator> { UuidIdGenerator }
        single<BandRepository> { RoomBandRepository(get(), get()) }
        single<SongRepository> { RoomSongRepository(get(), get()) }
        single<SetlistRepository> { RoomSetlistRepository(get(), get()) }
        single<EventRepository> { RoomEventRepository(get(), get()) }
        single<SettingsRepository> { RoomSettingsRepository(get()) }
        single<SetlistTransferRepository> { RoomSetlistTransferRepository(get(), get(), get()) }
        single<SetlistFileCodec> { JsonSetlistFileCodec() }
    }
