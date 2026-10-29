package com.tochlabs.setlist.core.data.db

import androidx.sqlite.SQLiteException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Foreign keys, cascades and ordering from design.md (decision 3) and the deletion rules
// of docs/product/data-model.md.
class SchemaIntegrityTest {
    private val db = inMemoryDatabase()

    @AfterTest
    fun close() = db.close()

    private suspend fun bandWithEverything() {
        db.bandDao().insert(bandRow("band"))
        db.songDao().insert(songRow("song"))
        db.repertoireDao().insertAll(listOf(RepertoireEntity("band", "song")))
        db.setlistDao().insert(setlistRow("setlist", "band"))
        db.setlistItemDao().insert(songItemRow("item", "setlist", "song", 0))
        db.eventDao().insert(eventRow("event", "band", setlistId = "setlist"))
        db.eventMilestoneDao().insert(milestoneRow("milestone", "event", LocalTime(18, 0)))
    }

    @Test
    fun deletingABandCascadesToItsDataButKeepsSongs() =
        runTest {
            bandWithEverything()

            db.bandDao().delete("band")

            assertNull(db.setlistDao().get("setlist"))
            assertNull(db.setlistItemDao().get("item"))
            assertNull(db.eventDao().get("event"))
            assertNull(db.eventMilestoneDao().get("milestone"))
            assertTrue(
                db
                    .repertoireDao()
                    .observeBandIds("song")
                    .first()
                    .isEmpty(),
            )
            assertNotNull(db.songDao().get("song"))
        }

    @Test
    fun aSongUsedByASetlistCannotBeDeleted() =
        runTest {
            bandWithEverything()

            assertFailsWith<SQLiteException> { db.songDao().delete("song") }
            assertNotNull(db.songDao().get("song"))
        }

    @Test
    fun deletingAnUnusedSongRemovesItsRepertoireEntries() =
        runTest {
            db.bandDao().insert(bandRow("band"))
            db.songDao().insert(songRow("song"))
            db.repertoireDao().insertAll(listOf(RepertoireEntity("band", "song")))

            db.songDao().delete("song")

            assertTrue(
                db
                    .repertoireDao()
                    .observeBandIds("song")
                    .first()
                    .isEmpty(),
            )
        }

    @Test
    fun deletingASetlistKeepsTheEventWithoutSetlist() =
        runTest {
            bandWithEverything()

            db.setlistDao().delete("setlist")

            assertNull(db.setlistItemDao().get("item"))
            assertNull(db.eventDao().get("event")?.setlistId)
            assertNotNull(db.eventDao().get("event"))
            assertNotNull(db.songDao().get("song"))
        }

    @Test
    fun deletingAnEventDeletesItsMilestones() =
        runTest {
            bandWithEverything()

            db.eventDao().delete("event")

            assertNull(db.eventMilestoneDao().get("milestone"))
        }

    @Test
    fun aSetlistCanBeLinkedToOnlyOneEvent() =
        runTest {
            bandWithEverything()

            assertFailsWith<SQLiteException> {
                db.eventDao().insert(eventRow("second", "band", setlistId = "setlist"))
            }
        }

    @Test
    fun itemsAreReadInPositionOrder() =
        runTest {
            bandWithEverything()
            db.setlistItemDao().insert(stopRow("third", "setlist", 2))
            db.setlistItemDao().insert(songItemRow("second", "setlist", "song", 1))

            val ids =
                db
                    .setlistItemDao()
                    .observeBySetlist("setlist")
                    .first()
                    .map { it.id }

            assertEquals(listOf("item", "second", "third"), ids)
        }

    @Test
    fun archivedBandsAreHiddenFromActiveLists() =
        runTest {
            bandWithEverything()
            db.bandDao().insert(bandRow("old", archivedAt = 10))
            db.setlistDao().insert(setlistRow("old-setlist", "old"))

            assertEquals(
                listOf("band"),
                db
                    .bandDao()
                    .observeActive()
                    .first()
                    .map { it.id },
            )
            assertEquals(
                listOf("setlist"),
                db
                    .setlistDao()
                    .observeActive()
                    .first()
                    .map { it.id },
            )
            assertEquals(
                listOf("event"),
                db
                    .eventDao()
                    .observeActive()
                    .first()
                    .map { it.id },
            )
        }
}
