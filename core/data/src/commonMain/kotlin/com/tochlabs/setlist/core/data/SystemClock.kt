package com.tochlabs.setlist.core.data

import com.tochlabs.setlist.core.domain.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Instant
import kotlin.time.Clock as KotlinClock

/** The real clock. Domain code receives it through the [Clock] interface. */
internal object SystemClock : Clock {
    override fun now(): Instant = KotlinClock.System.now()

    override fun today(): LocalDate = KotlinClock.System.todayIn(TimeZone.currentSystemDefault())
}
