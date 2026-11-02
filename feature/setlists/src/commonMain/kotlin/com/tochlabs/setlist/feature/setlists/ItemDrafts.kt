package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.domain.validation.SHORT_TEXT_MAX_LENGTH
import com.tochlabs.setlist.core.domain.validation.limitShortText
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.StopType

/**
 * What the item editor sheet edits for one song of a setlist (specs/setlists, "Per-setlist overrides"
 * and "Links between songs"). Fields are typed text; [applyTo] converts them.
 */
data class SongItemDraft(
    val item: SetlistItem.SongItem,
    val song: Song,
    /** The link only takes effect when the next item is a song. */
    val nextIsSong: Boolean,
    val key: String = item.keyOverride.orEmpty(),
    val bpm: String = item.bpmOverride?.toString().orEmpty(),
    val instrument: String = item.instrument.orEmpty(),
    val tuning: String = item.tuningOverride.orEmpty(),
    val note: String = item.note.orEmpty(),
    val linked: Boolean = item.linkedToNext,
    val transition: String = item.transitionText.orEmpty(),
) {
    val originalKey: String? get() = song.originalKey
    val originalBpm: Int? get() = song.originalBpm

    /** Null means standard tuning. */
    val originalTuning: String? get() = song.originalTuning

    fun withKey(text: String) = copy(key = text)

    fun withBpm(text: String) = copy(bpm = text.filter { it.isDigit() }.take(MAX_BPM_DIGITS))

    /** At most 20 characters: extra characters are dropped as they are typed. */
    fun withInstrument(text: String) = copy(instrument = text.take(SHORT_TEXT_MAX_LENGTH))

    fun withTuning(text: String) = copy(tuning = text.take(SHORT_TEXT_MAX_LENGTH))

    fun withNote(text: String) = copy(note = text)

    fun withLinked(linked: Boolean) = copy(linked = linked)

    fun withTransition(text: String) = copy(transition = text)

    /** Blank fields mean "no override": the song's original value is used. */
    internal fun applyTo(): SetlistItem.SongItem =
        item.copy(
            keyOverride = key.trim().ifEmpty { null },
            bpmOverride = bpm.toIntOrNull()?.takeIf { it > 0 },
            instrument = limitShortText(instrument),
            tuningOverride = limitShortText(tuning),
            note = note.trim().ifEmpty { null },
            linkedToNext = linked,
            transitionText = transition.trim().ifEmpty { null },
        )
}

/** A new stop ([itemId] null) or the edit of an existing one. */
data class StopDraft(
    val itemId: SetlistItemId? = null,
    val type: StopType = StopType.SPEECH,
    val durationSeconds: Int = DEFAULT_STOP_SECONDS,
    val note: String = "",
) {
    fun withType(type: StopType) = copy(type = type)

    fun withDuration(seconds: Int) = copy(durationSeconds = seconds.coerceIn(0, MAX_STOP_SECONDS))

    /** Stepper: adds or removes [deltaSeconds], staying within 0 and the maximum. */
    fun adjust(deltaSeconds: Int) = withDuration(durationSeconds + deltaSeconds)

    fun withNote(text: String) = copy(note = text)

    internal fun toItem(id: SetlistItemId) = SetlistItem.Stop(id, type, durationSeconds, note.trim().ifEmpty { null })

    companion object {
        fun of(stop: SetlistItem.Stop) = StopDraft(stop.id, stop.type, stop.durationSeconds, stop.note.orEmpty())
    }
}

private const val MAX_BPM_DIGITS = 3
internal const val DEFAULT_STOP_SECONDS = 60
internal const val MAX_STOP_SECONDS = 99 * 60
