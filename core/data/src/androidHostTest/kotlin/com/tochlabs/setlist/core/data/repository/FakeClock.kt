package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.domain.time.Clock
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal class FakeClock(
    var instant: Instant = Instant.fromEpochMilliseconds(1_000),
    var date: LocalDate = LocalDate(2026, 10, 29),
) : Clock {
    override fun now(): Instant = instant

    override fun today(): LocalDate = date
}
