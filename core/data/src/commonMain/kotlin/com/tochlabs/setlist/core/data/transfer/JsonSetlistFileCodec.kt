package com.tochlabs.setlist.core.data.transfer

import com.tochlabs.setlist.core.domain.transfer.SetlistFileCodec
import com.tochlabs.setlist.core.domain.transfer.SetlistFileResult
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.limitShortText
import com.tochlabs.setlist.core.domain.validation.validatePause
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SetlistPackage
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant

/**
 * `.setlist` files as UTF-8 JSON (design.md, decision 8). Version "major.minor": a newer minor
 * version can be read (unknown fields are ignored), a newer major version cannot.
 */
class JsonSetlistFileCodec : SetlistFileCodec {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    override fun encode(pkg: SetlistPackage): String = json.encodeToString(FileDto.serializer(), pkg.toDto())

    override fun decode(text: String): SetlistFileResult =
        try {
            val root = json.parseToJsonElement(text).jsonObject
            val version = root["formatVersion"]?.jsonPrimitive?.content.orEmpty()
            val major = version.substringBefore('.').toIntOrNull()
            when {
                major == null || major < FORMAT_MAJOR -> SetlistFileResult.Unreadable
                major > FORMAT_MAJOR -> SetlistFileResult.NewerVersion
                else -> SetlistFileResult.Valid(json.decodeFromJsonElement(FileDto.serializer(), root).toPackage())
            }
        } catch (ignored: IllegalArgumentException) {
            // Malformed JSON, missing fields and failed checks all mean the file cannot be read.
            SetlistFileResult.Unreadable
        }

    private companion object {
        const val FORMAT_MAJOR = 1
        const val FORMAT_VERSION = "1.0"
    }

    private fun SetlistPackage.toDto() =
        FileDto(
            formatVersion = FORMAT_VERSION,
            exportedAt = exportedAt.toString(),
            band = bandName,
            setlist =
                SetlistDto(
                    name = setlist.name,
                    date = setlist.date?.toString(),
                    pauseSeconds = setlist.pauseSeconds,
                    items = items.map { it.toDto() },
                ),
            songs =
                songs.map {
                    SongDto(
                        it.id.value,
                        it.title,
                        it.artist,
                        it.originalKey,
                        it.originalBpm,
                        it.originalTuning,
                        it.durationSeconds,
                    )
                },
        )

    private fun SetlistItem.toDto() =
        when (this) {
            is SetlistItem.SongItem ->
                ItemDto(
                    type = TYPE_SONG,
                    song = songId.value,
                    key = keyOverride,
                    bpm = bpmOverride,
                    instrument = instrument,
                    tuning = tuningOverride,
                    note = note,
                    linkedToNext = linkedToNext,
                    transition = transitionText,
                )
            is SetlistItem.Stop ->
                ItemDto(type = TYPE_STOP, stop = type.name.lowercase(), durationSeconds = durationSeconds, note = note)
        }
}

private const val TYPE_SONG = "song"
private const val TYPE_STOP = "stop"

/** Ids given to what a file contains; importing replaces them. */
internal val FILE_SETLIST_ID = SetlistId("setlist")
internal val FILE_BAND_ID = BandId("band")

internal fun fileItemId(index: Int) = SetlistItemId("i${index + 1}")

internal fun fileSongId(index: Int) = SongId("s${index + 1}")

// Validation throws IllegalArgumentException (require), which decode turns into Unreadable.

private fun FileDto.toPackage(): SetlistPackage {
    val pause = setlist.pauseSeconds
    require(validatePause(pause) is Validated.Valid)
    val songs = songs.map { it.toSong() }
    val songIds = songs.map { it.id }.toSet()
    require(songIds.size == songs.size) { "duplicate song ids" }
    return SetlistPackage(
        bandName = band,
        setlist =
            Setlist(
                id = FILE_SETLIST_ID,
                bandId = FILE_BAND_ID,
                name = required(setlist.name),
                date = setlist.date?.let(LocalDate::parse),
                pauseSeconds = pause,
            ),
        items = setlist.items.mapIndexed { index, item -> item.toItem(fileItemId(index), songIds) },
        songs = songs,
        exportedAt = Instant.parse(exportedAt),
    )
}

private fun SongDto.toSong(): Song {
    require(bpm == null || bpm > 0)
    require(durationSeconds == null || durationSeconds >= 0)
    return Song(
        id = SongId(id),
        title = required(title),
        artist = artist?.trim()?.ifEmpty { null },
        originalKey = key?.trim()?.ifEmpty { null },
        originalBpm = bpm,
        originalTuning = limitShortText(tuning),
        durationSeconds = durationSeconds?.takeIf { it > 0 },
    )
}

private fun ItemDto.toItem(
    id: SetlistItemId,
    songIds: Set<SongId>,
): SetlistItem =
    when (type) {
        TYPE_SONG -> {
            val songId = SongId(requireNotNull(song))
            require(songId in songIds) { "item refers to a missing song" }
            require(bpm == null || bpm > 0)
            SetlistItem.SongItem(
                id = id,
                songId = songId,
                keyOverride = key?.trim()?.ifEmpty { null },
                bpmOverride = bpm,
                instrument = limitShortText(instrument),
                tuningOverride = limitShortText(tuning),
                note = note?.trim()?.ifEmpty { null },
                linkedToNext = linkedToNext,
                transitionText = transition?.trim()?.ifEmpty { null },
            )
        }
        TYPE_STOP -> {
            val stopType = requireNotNull(StopType.entries.find { it.name.lowercase() == stop }) { "unknown stop type" }
            val seconds = requireNotNull(durationSeconds)
            require(seconds >= 0)
            SetlistItem.Stop(id, stopType, seconds, note?.trim()?.ifEmpty { null })
        }
        else -> throw IllegalArgumentException("unknown item type")
    }

private fun required(text: String): String {
    val valid = validateRequiredText(text)
    require(valid is Validated.Valid)
    return valid.value
}

@Serializable
private data class FileDto(
    val formatVersion: String,
    val exportedAt: String,
    val band: String,
    val setlist: SetlistDto,
    val songs: List<SongDto>,
)

@Serializable
private data class SetlistDto(
    val name: String,
    val date: String? = null,
    val pauseSeconds: Int,
    val items: List<ItemDto>,
)

/** A song (`type` "song", `song` is a file song id) or a stop (`type` "stop" with a `stop` code). */
@Serializable
private data class ItemDto(
    val type: String,
    val song: String? = null,
    val key: String? = null,
    val bpm: Int? = null,
    val instrument: String? = null,
    val tuning: String? = null,
    val note: String? = null,
    val linkedToNext: Boolean = false,
    val transition: String? = null,
    val stop: String? = null,
    val durationSeconds: Int? = null,
)

@Serializable
private data class SongDto(
    val id: String,
    val title: String,
    val artist: String? = null,
    val key: String? = null,
    val bpm: Int? = null,
    val tuning: String? = null,
    val durationSeconds: Int? = null,
)
