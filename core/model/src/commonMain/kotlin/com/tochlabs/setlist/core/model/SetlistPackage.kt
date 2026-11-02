package com.tochlabs.setlist.core.model

import kotlin.time.Instant

/**
 * Everything a `.setlist` file carries (specs/setlist-sharing): the setlist, its items in order and
 * the catalogue data of every song used. Ids are local to the file; importing gives new ones.
 */
data class SetlistPackage(
    /** Informational: the receiver chooses the band to import into. */
    val bandName: String,
    val setlist: Setlist,
    val items: List<SetlistItem>,
    /** Each song used by the items, once. */
    val songs: List<Song>,
    val exportedAt: Instant,
) {
    val songCount: Int get() = items.count { it is SetlistItem.SongItem }
    val stopCount: Int get() = items.count { it is SetlistItem.Stop }
}
