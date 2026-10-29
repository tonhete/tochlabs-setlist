package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.SongId

data class Change<out T>(
    val from: T,
    val to: T,
)

/** Markers of one song item (specs/setlists). Stops have none. */
data class ItemMarkers(
    /** New instrument when it differs from the previous song's. */
    val instrumentChangeTo: String? = null,
    /** Tuning against the previous song (standard for the first song). Null tuning = standard. */
    val tuningChange: Change<String?>? = null,
    /** Original key to setlist key. */
    val keyChange: Change<String?>? = null,
    /** Original tempo to setlist tempo. */
    val tempoChange: Change<Int?>? = null,
    val isRepeated: Boolean = false,
    val isLinkedToNext: Boolean = false,
    val isLinkedFromPrevious: Boolean = false,
    /** Only present when the link takes effect. */
    val transitionText: String? = null,
)

fun computeMarkers(items: List<ResolvedItem>): List<ItemMarkers?> {
    val seen = mutableSetOf<SongId>()
    var previous: ResolvedItem.SongEntry? = null
    return items.mapIndexed { index, entry ->
        if (entry !is ResolvedItem.SongEntry) return@mapIndexed null
        val linked = items.isLinkEffective(index)
        val markers =
            ItemMarkers(
                instrumentChangeTo = instrumentChange(previous, entry),
                tuningChange = tuningChange(previous?.effectiveTuning, entry.effectiveTuning),
                keyChange = overrideChange(entry.song.originalKey, entry.item.keyOverride),
                tempoChange = overrideChange(entry.song.originalBpm, entry.item.bpmOverride),
                isRepeated = !seen.add(entry.song.id),
                isLinkedToNext = linked,
                isLinkedFromPrevious = index > 0 && items.isLinkEffective(index - 1),
                transitionText = if (linked) entry.item.transitionText else null,
            )
        previous = entry
        markers
    }
}

private fun instrumentChange(
    previous: ResolvedItem.SongEntry?,
    current: ResolvedItem.SongEntry,
): String? {
    val instrument = current.item.instrument
    val changed = previous != null && instrument != null && !instrument.sameText(previous.item.instrument)
    return if (changed) instrument else null
}

private fun tuningChange(
    previous: String?,
    current: String?,
): Change<String?>? = if (current.sameText(previous)) null else Change(previous, current)

private fun <T> overrideChange(
    original: T?,
    override: T?,
): Change<T?>? = if (override == null || override == original) null else Change(original, override)

private fun String?.sameText(other: String?): Boolean =
    this?.trim().orEmpty().equals(other?.trim().orEmpty(), ignoreCase = true)
