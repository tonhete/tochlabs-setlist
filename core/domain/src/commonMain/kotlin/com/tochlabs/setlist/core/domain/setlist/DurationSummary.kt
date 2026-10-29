package com.tochlabs.setlist.core.domain.setlist

data class DurationSummary(
    val musicSeconds: Long,
    val stopsSeconds: Long,
    val pausesSeconds: Long,
    val pauseCount: Int,
    val linkedPairs: Int,
    val songsWithoutDuration: Int,
) {
    val totalWithStopsSeconds: Long get() = musicSeconds + stopsSeconds + pausesSeconds

    /** What the music-only figure leaves out. */
    val leftOutOfMusicOnlySeconds: Long get() = stopsSeconds + pausesSeconds

    val isComplete: Boolean get() = songsWithoutDuration == 0
}

/**
 * Totals of a setlist (specs/setlist-duration). One pause per pair of consecutive items,
 * songs or stops, unless the pair is an effective link between two songs.
 */
fun calculateDuration(
    items: List<ResolvedItem>,
    pauseSeconds: Int,
): DurationSummary {
    var music = 0L
    var stops = 0L
    var withoutDuration = 0
    items.forEach { entry ->
        when (entry) {
            is ResolvedItem.SongEntry -> {
                val seconds = entry.song.durationSeconds
                if (seconds == null) withoutDuration++ else music += seconds
            }
            is ResolvedItem.StopEntry -> stops += entry.item.durationSeconds
        }
    }
    val pairs = (items.size - 1).coerceAtLeast(0)
    val linked = (0 until pairs).count { items.isLinkEffective(it) }
    val pauses = pairs - linked
    return DurationSummary(
        musicSeconds = music,
        stopsSeconds = stops,
        pausesSeconds = pauses.toLong() * pauseSeconds,
        pauseCount = pauses,
        linkedPairs = linked,
        songsWithoutDuration = withoutDuration,
    )
}
