package com.tochlabs.setlist.core.domain.repository

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    /** Events of active bands by date; with a band, only that band's. */
    fun observeEvents(bandId: BandId? = null): Flow<List<Event>>

    fun observe(id: EventId): Flow<Event?>

    fun observeBySetlist(setlistId: SetlistId): Flow<Event?>

    /** Milestones by start time. */
    fun observeMilestones(eventId: EventId): Flow<List<EventMilestone>>

    suspend fun save(event: Event)

    /** Deletes the event and its milestones. */
    suspend fun delete(id: EventId)

    suspend fun saveMilestone(milestone: EventMilestone)

    suspend fun deleteMilestone(id: EventMilestoneId)
}
