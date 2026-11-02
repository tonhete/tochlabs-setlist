package com.tochlabs.setlist.feature.calendar

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.EventId
import kotlinx.datetime.LocalDate
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calendarModule =
    module {
        viewModelOf(::CalendarViewModel)
        // Parameters: event to edit (null for a new one), then date (ISO text) and band to prefill.
        viewModel { params ->
            EventFormViewModel(
                (params.values.getOrNull(0) as String?)?.let(::EventId),
                (params.values.getOrNull(1) as String?)?.let(LocalDate::parse),
                (params.values.getOrNull(2) as String?)?.let(::BandId),
                get(),
                get(),
                get(),
            )
        }
        viewModel { params -> EventDetailViewModel(EventId(params.get<String>(0)), get(), get(), get(), get(), get()) }
    }
