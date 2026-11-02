package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId

/** A setlist item together with the data it needs, so rules run without a repository. */
sealed interface ResolvedItem {
    data class SongEntry(
        val item: SetlistItem.SongItem,
        val song: Song,
    ) : ResolvedItem

    data class StopEntry(
        val item: SetlistItem.Stop,
    ) : ResolvedItem
}

/** A link only takes effect when a song is followed by another song. */
fun List<ResolvedItem>.isLinkEffective(index: Int): Boolean {
    val current = getOrNull(index) as? ResolvedItem.SongEntry ?: return false
    return current.item.linkedToNext && getOrNull(index + 1) is ResolvedItem.SongEntry
}

/** Pairs each item with its song, keeping the order; items whose song is missing are left out. */
fun resolveItems(
    items: List<SetlistItem>,
    songs: Map<SongId, Song>,
): List<ResolvedItem> =
    items.mapNotNull { item ->
        when (item) {
            is SetlistItem.SongItem -> songs[item.songId]?.let { ResolvedItem.SongEntry(item, it) }
            is SetlistItem.Stop -> ResolvedItem.StopEntry(item)
        }
    }
