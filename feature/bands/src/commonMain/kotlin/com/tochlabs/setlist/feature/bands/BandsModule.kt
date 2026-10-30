package com.tochlabs.setlist.feature.bands

import com.tochlabs.setlist.core.model.BandId
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bandsModule =
    module {
        viewModelOf(::BandsViewModel)
        viewModelOf(::BandHistoryViewModel)
        // Parameter: the id of the band to edit, or null for a new band.
        viewModel { params ->
            BandFormViewModel(params.values.firstOrNull()?.let { BandId(it as String) }, get(), get())
        }
    }
