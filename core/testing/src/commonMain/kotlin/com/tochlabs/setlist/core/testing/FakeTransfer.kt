package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.text.sameSong
import com.tochlabs.setlist.core.domain.time.Clock
import com.tochlabs.setlist.core.domain.transfer.SetlistFileCodec
import com.tochlabs.setlist.core.domain.transfer.SetlistFileResult
import com.tochlabs.setlist.core.domain.transfer.SetlistTransferRepository
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SetlistPackage
import com.tochlabs.setlist.core.model.SongId
import kotlinx.coroutines.flow.update

/** Export and import over [FakeStore], with the same matching rules as the database version. */
class FakeSetlistTransferRepository(
    private val store: FakeStore,
    private val ids: IdGenerator = SequentialIdGenerator(),
    private val clock: Clock = FakeClock(),
) : SetlistTransferRepository {
    override suspend fun exportPackage(setlistId: SetlistId): SetlistPackage? {
        val setlist = store.setlists.value.find { it.id == setlistId } ?: return null
        val items = store.items.value[setlistId].orEmpty()
        val used = items.filterIsInstance<SetlistItem.SongItem>().map { it.songId }.distinct()
        return SetlistPackage(
            bandName =
                store.bands.value
                    .find { it.id == setlist.bandId }
                    ?.name
                    .orEmpty(),
            setlist = setlist,
            items = items,
            songs = used.mapNotNull { id -> store.songs.value.find { it.id == id } },
            exportedAt = clock.now(),
        )
    }

    override suspend fun import(
        pkg: SetlistPackage,
        bandId: BandId,
    ): SetlistId {
        val songIds =
            pkg.songs.associate { song ->
                val existing = store.songs.value.find { sameSong(it.title, it.artist, song.title, song.artist) }
                val id =
                    existing?.id ?: SongId(ids.newId()).also { newId ->
                        store.songs.update { it + song.copy(id = newId) }
                        store.repertoire.update { it + (bandId to newId) }
                    }
                song.id to id
            }
        val setlistId = SetlistId(ids.newId())
        store.setlists.update { it + pkg.setlist.copy(id = setlistId, bandId = bandId) }
        val items =
            pkg.items.map { item ->
                when (item) {
                    is SetlistItem.SongItem ->
                        item.copy(
                            id = SetlistItemId(ids.newId()),
                            songId = songIds.getValue(item.songId),
                        )
                    is SetlistItem.Stop -> item.copy(id = SetlistItemId(ids.newId()))
                }
            }
        store.items.update { it + (setlistId to items) }
        return setlistId
    }
}

/** Remembers what it encodes; the texts [UNREADABLE] and [NEWER] decode to the two failures. */
class FakeSetlistFileCodec : SetlistFileCodec {
    private val encoded = mutableMapOf<String, SetlistPackage>()

    override fun encode(pkg: SetlistPackage): String =
        "setlist:${encoded.size}:${pkg.setlist.name}".also {
            encoded[it] =
                pkg
        }

    override fun decode(text: String): SetlistFileResult =
        when {
            text == NEWER -> SetlistFileResult.NewerVersion
            else -> encoded[text]?.let { SetlistFileResult.Valid(it) } ?: SetlistFileResult.Unreadable
        }

    companion object {
        const val UNREADABLE = "garbage"
        const val NEWER = "from-the-future"
    }
}
