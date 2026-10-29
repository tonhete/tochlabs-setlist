package com.tochlabs.setlist.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Event(
    val id: EventId,
    val bandId: BandId,
    val type: EventType,
    val date: LocalDate,
    val title: String,
    val venue: String? = null,
    val address: String? = null,
    val parking: String? = null,
    val contact: String? = null,
    val setlistId: SetlistId? = null,
)

enum class EventType { GIG, REHEARSAL }

data class EventMilestone(
    val id: EventMilestoneId,
    val eventId: EventId,
    val kind: MilestoneKind,
    /** Required when [kind] is [MilestoneKind.CUSTOM]. */
    val customLabel: String? = null,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
)

enum class MilestoneKind { ARRIVAL, BACKLINE, SOUNDCHECK, ON_STAGE, GIG, PACK_UP, DEPARTURE, CUSTOM }
