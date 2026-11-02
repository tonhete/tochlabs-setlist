package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val setlistsModule =
    module {
        viewModelOf(::SetlistsViewModel)
        // Parameters: the id of the setlist to edit (null for a new one) and the band to preselect.
        viewModel { params ->
            SetlistFormViewModel(
                (params.values.getOrNull(0) as String?)?.let(::SetlistId),
                (params.values.getOrNull(1) as String?)?.let(::BandId),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel { params ->
            SetlistDetailViewModel(SetlistId(params.get<String>(0)), get(), get(), get(), get(), get())
        }
        viewModel { params -> AddSongViewModel(SetlistId(params.get<String>(0)), get(), get(), get()) }
    }
