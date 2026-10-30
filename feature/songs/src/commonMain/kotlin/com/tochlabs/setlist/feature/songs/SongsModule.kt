package com.tochlabs.setlist.feature.songs

import com.tochlabs.setlist.core.model.SongId
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val songsModule =
    module {
        viewModelOf(::SongsViewModel)
        // Parameter: the id of the song to edit, or null for a new song.
        viewModel { params ->
            SongFormViewModel(params.values.firstOrNull()?.let { SongId(it as String) }, get(), get(), get())
        }
    }
