package com.tochlabs.setlist.core.domain.setlist

// Effective value of a setlist item: the override if present, otherwise the song's original.

val ResolvedItem.SongEntry.effectiveKey: String? get() = item.keyOverride ?: song.originalKey

val ResolvedItem.SongEntry.effectiveBpm: Int? get() = item.bpmOverride ?: song.originalBpm

/** Null means standard tuning. */
val ResolvedItem.SongEntry.effectiveTuning: String? get() = item.tuningOverride ?: song.originalTuning
