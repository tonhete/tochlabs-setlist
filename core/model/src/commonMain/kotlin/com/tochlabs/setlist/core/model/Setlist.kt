package com.tochlabs.setlist.core.model

import kotlinx.datetime.LocalDate

data class Setlist(
    val id: SetlistId,
    val bandId: BandId,
    val name: String,
    val date: LocalDate? = null,
    val pauseSeconds: Int,
)

/** One entry of a setlist. Order is the position in the list that holds it. */
sealed interface SetlistItem {
    val id: SetlistItemId
    val note: String?

    data class SongItem(
        override val id: SetlistItemId,
        val songId: SongId,
        val keyOverride: String? = null,
        val bpmOverride: Int? = null,
        val instrument: String? = null,
        val tuningOverride: String? = null,
        override val note: String? = null,
        val linkedToNext: Boolean = false,
        val transitionText: String? = null,
    ) : SetlistItem

    data class Stop(
        override val id: SetlistItemId,
        val type: StopType,
        val durationSeconds: Int,
        override val note: String? = null,
    ) : SetlistItem
}

enum class StopType { SPEECH, BREAK, TUNING, ENCORE }
