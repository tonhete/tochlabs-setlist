package com.tochlabs.setlist.core.data.db

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.SetlistId

internal fun EventEntity.toModel() =
    Event(
        id = EventId(id),
        bandId = BandId(bandId),
        type = type,
        date = date,
        title = title,
        venue = venue,
        address = address,
        parking = parking,
        contact = contact,
        setlistId = setlistId?.let(::SetlistId),
    )

internal fun Event.toEntity(time: Timestamps) =
    EventEntity(
        id = id.value,
        bandId = bandId.value,
        type = type,
        date = date,
        title = title,
        venue = venue,
        address = address,
        parking = parking,
        contact = contact,
        setlistId = setlistId?.value,
        createdAt = time.createdAt,
        updatedAt = time.updatedAt,
    )

internal fun EventMilestoneEntity.toModel() =
    EventMilestone(
        id = EventMilestoneId(id),
        eventId = EventId(eventId),
        kind = kind,
        customLabel = customLabel,
        startTime = startTime,
        endTime = endTime,
    )

internal fun EventMilestone.toEntity(time: Timestamps) =
    EventMilestoneEntity(
        id = id.value,
        eventId = eventId.value,
        kind = kind,
        customLabel = customLabel,
        startTime = startTime,
        endTime = endTime,
        createdAt = time.createdAt,
        updatedAt = time.updatedAt,
    )
