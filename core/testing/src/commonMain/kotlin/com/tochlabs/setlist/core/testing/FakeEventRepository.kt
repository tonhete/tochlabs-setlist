package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeEventRepository(
    private val store: FakeStore,
) : EventRepository {
    override fun observeEvents(bandId: BandId?): Flow<List<Event>> =
        combine(store.events, store.bands) { events, bands ->
            val active = bands.filterNot { it.isArchived }.map { it.id }.toSet()
            events.filter { it.bandId in active && (bandId == null || it.bandId == bandId) }.sortedBy { it.date }
        }

    override fun observe(id: EventId): Flow<Event?> = store.events.map { list -> list.find { it.id == id } }

    override fun observeBySetlist(setlistId: SetlistId): Flow<Event?> =
        store.events.map { list -> list.find { it.setlistId == setlistId } }

    override fun observeMilestones(eventId: EventId): Flow<List<EventMilestone>> =
        store.milestones.map { list -> list.filter { it.eventId == eventId }.sortedBy { it.startTime } }

    override suspend fun save(event: Event) = store.events.upsert(event) { it.id == event.id }

    override suspend fun delete(id: EventId) {
        store.events.update { list -> list.filterNot { it.id == id } }
        store.milestones.update { list -> list.filterNot { it.eventId == id } }
    }

    override suspend fun saveMilestone(milestone: EventMilestone) =
        store.milestones.upsert(milestone) { it.id == milestone.id }

    override suspend fun deleteMilestone(id: EventMilestoneId) =
        store.milestones.update { list -> list.filterNot { it.id == id } }
}
