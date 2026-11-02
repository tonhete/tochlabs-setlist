package com.tochlabs.setlist.feature.stage

import com.tochlabs.setlist.core.model.SetlistId
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val stageModule =
    module {
        // Parameter: the id of the setlist to show.
        viewModel { params -> StageViewModel(SetlistId(params.get<String>(0)), get(), get()) }
    }
