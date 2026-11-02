package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory tables shared by the fake repositories, so a change made through one repository
 * is seen by the others, as with the real database.
 */
class FakeStore {
    val bands = MutableStateFlow<List<Band>>(emptyList())
    val songs = MutableStateFlow<List<Song>>(emptyList())

    /** Repertoire entries: which bands play which songs. */
    val repertoire = MutableStateFlow<Set<Pair<BandId, SongId>>>(emptySet())
    val setlists = MutableStateFlow<List<Setlist>>(emptyList())
    val items = MutableStateFlow<Map<SetlistId, List<SetlistItem>>>(emptyMap())
    val events = MutableStateFlow<List<Event>>(emptyList())
    val milestones = MutableStateFlow<List<EventMilestone>>(emptyList())

    fun activeBandIds(): Set<BandId> =
        bands.value
            .filterNot { it.isArchived }
            .map { it.id }
            .toSet()

    /** Removes a band and everything that cascades from it in the database. Songs stay. */
    fun cascadeDeleteBand(id: BandId) {
        val setlistIds =
            setlists.value
                .filter { it.bandId == id }
                .map { it.id }
                .toSet()
        bands.update { list -> list.filterNot { it.id == id } }
        setlists.update { list -> list.filterNot { it.bandId == id } }
        items.update { map -> map - setlistIds }
        val eventIds =
            events.value
                .filter { it.bandId == id }
                .map { it.id }
                .toSet()
        events.update { list -> list.filterNot { it.bandId == id } }
        milestones.update { list -> list.filterNot { it.eventId in eventIds } }
        repertoire.update { set -> set.filterNot { it.first == id }.toSet() }
    }
}

internal fun <T> MutableStateFlow<List<T>>.upsert(
    value: T,
    sameKey: (T) -> Boolean,
) = update { list -> if (list.any(sameKey)) list.map { if (sameKey(it)) value else it } else list + value }
