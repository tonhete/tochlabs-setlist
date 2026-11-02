package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolvedItemTest {
    @Test
    fun songsArePairedWithTheirSongAndStopsKeptInOrder() {
        val faro = song(200, id = "faro")
        val items =
            listOf(
                SetlistItem.SongItem(SetlistItemId("1"), faro.id),
                SetlistItem.Stop(SetlistItemId("2"), StopType.SPEECH, 60),
            )

        val resolved = resolveItems(items, mapOf(faro.id to faro))

        assertEquals(
            listOf(
                ResolvedItem.SongEntry(items[0] as SetlistItem.SongItem, faro),
                ResolvedItem.StopEntry(items[1] as SetlistItem.Stop),
            ),
            resolved,
        )
    }

    @Test
    fun anItemWhoseSongIsMissingIsLeftOut() {
        val items = listOf(SetlistItem.SongItem(SetlistItemId("1"), SongId("gone")))
        assertEquals(emptyList(), resolveItems(items, emptyMap()))
    }
}
