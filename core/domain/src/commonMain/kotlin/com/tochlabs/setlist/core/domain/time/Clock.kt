package com.tochlabs.setlist.core.domain.time

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Source of the current time. Injected so rules that depend on "today" stay testable. */
interface Clock {
    fun now(): Instant

    /** Today's date in the device time zone. */
    fun today(): LocalDate
}
