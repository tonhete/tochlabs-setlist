package com.tochlabs.setlist.feature.share

import com.tochlabs.setlist.core.model.SetlistId
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shareModule =
    module {
        includes(sharePlatformModule)
        single { IncomingSetlistFiles() }
        single<ExportTextsProvider> { ResourceExportTexts }
        // Parameter: the id of the setlist to export.
        viewModel { params -> ExportViewModel(SetlistId(params.get<String>(0)), get(), get(), get(), get()) }
        viewModel { ImportViewModel(get(), get(), get(), get()) }
    }
