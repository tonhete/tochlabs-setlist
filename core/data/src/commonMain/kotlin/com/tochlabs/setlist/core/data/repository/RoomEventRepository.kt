package com.tochlabs.setlist.core.data.repository

import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.data.db.Timestamps
import com.tochlabs.setlist.core.data.db.toEntity
import com.tochlabs.setlist.core.data.db.toModel
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomEventRepository(
    private val db: SetlistDatabase,
    private val clock: Clock,
) : EventRepository {
    private val dao = db.eventDao()
    private val milestones = db.eventMilestoneDao()

    override fun observeEvents(bandId: BandId?): Flow<List<Event>> {
        val rows = if (bandId == null) dao.observeActive() else dao.observeActiveByBand(bandId.value)
        return rows.map { list -> list.map { it.toModel() } }
    }

    override fun observe(id: EventId): Flow<Event?> = dao.observe(id.value).map { it?.toModel() }

    override fun observeBySetlist(setlistId: SetlistId): Flow<Event?> =
        dao.observeBySetlist(setlistId.value).map { it?.toModel() }

    override fun observeMilestones(eventId: EventId): Flow<List<EventMilestone>> =
        milestones.observeByEvent(eventId.value).map { rows -> rows.map { it.toModel() } }

    override suspend fun save(event: Event) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val existing = dao.get(event.id.value)
            val row = event.toEntity(Timestamps(existing?.createdAt ?: now, now))
            if (existing == null) dao.insert(row) else dao.update(row)
        }
    }

    /** Milestones cascade. */
    override suspend fun delete(id: EventId) {
        dao.delete(id.value)
    }

    override suspend fun saveMilestone(milestone: EventMilestone) {
        db.withWriteTransaction {
            val now = clock.nowMillis()
            val existing = milestones.get(milestone.id.value)
            val row = milestone.toEntity(Timestamps(existing?.createdAt ?: now, now))
            if (existing == null) milestones.insert(row) else milestones.update(row)
        }
    }

    override suspend fun deleteMilestone(id: EventMilestoneId) {
        milestones.delete(id.value)
    }
}
