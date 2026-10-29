package com.tochlabs.setlist.core.model

data class Song(
    val id: SongId,
    val title: String,
    val artist: String? = null,
    val originalKey: String? = null,
    val originalBpm: Int? = null,
    /** Null means standard tuning. */
    val originalTuning: String? = null,
    /** Null means "no duration"; totals that include it are incomplete. */
    val durationSeconds: Int? = null,
)
