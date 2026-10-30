package com.tochlabs.setlist.feature.songs

import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/song-catalog: "Create and edit songs", "Optional duration", "Free-text tuning",
// "Repertoire per band", "Delete a song only when unused".
class SongFormViewModelTest {
    @Test
    fun createASongWithAllData() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            val form = env.formViewModel()

            form.onTitleChange(" Faro ")
            form.onArtistChange("Mareas")
            form.onKeyChange("Am")
            form.onBpmChange("120")
            form.onTuningChange("Drop D")
            form.onMinutesChange("3")
            form.onSecondsChange("45")
            form.toggleBand(mareas.id)
            form.save()

            assertTrue(form.state.value.saved)
            val song =
                env.store.songs.value
                    .single()
            assertEquals(
                Song(SongId("id-1"), "Faro", "Mareas", "Am", 120, "Drop D", 225),
                song,
            )
            assertEquals(setOf(mareas.id), env.songs.observeBandsOf(song.id).first())
        }

    @Test
    fun createASongWithTitleOnly_missingFieldsAreEmpty() =
        runViewModelTest {
            val env = SongsTestEnv()
            val form = env.formViewModel()

            form.onTitleChange("Faro")
            form.save()

            assertEquals(
                Song(SongId("id-1"), "Faro"),
                env.store.songs.value
                    .single(),
            )
        }

    @Test
    fun rejectAnEmptyTitle() =
        runViewModelTest {
            val env = SongsTestEnv()
            val form = env.formViewModel()

            form.onArtistChange("Mareas")
            form.save()

            assertTrue(form.state.value.titleError)
            assertFalse(form.state.value.saved)
            assertTrue(
                env.store.songs.value
                    .isEmpty(),
            )
        }

    @Test
    fun saveWithoutDuration_theUserIsToldTotalsWillBeIncomplete() =
        runViewModelTest {
            val form = SongsTestEnv().formViewModel()
            form.onTitleChange("Faro")
            assertTrue(form.state.value.missingDuration)

            form.onSecondsChange("30")
            assertFalse(form.state.value.missingDuration)
        }

    @Test
    fun secondsMustBeBelowSixty() =
        runViewModelTest {
            val env = SongsTestEnv()
            val form = env.formViewModel()
            form.onTitleChange("Faro")
            form.onMinutesChange("3")
            form.onSecondsChange("75")

            form.save()

            assertTrue(form.state.value.durationError)
            assertTrue(
                env.store.songs.value
                    .isEmpty(),
            )
        }

    @Test
    fun numericFieldsAcceptOnlyDigits() =
        runViewModelTest {
            val form = SongsTestEnv().formViewModel()
            form.onBpmChange("12a0")
            form.onMinutesChange("-3")
            assertEquals("120", form.state.value.bpm)
            assertEquals("3", form.state.value.minutes)
        }

    @Test
    fun tuningTooLong_keepsOnlyTheFirst20Characters() =
        runViewModelTest {
            val form = SongsTestEnv().formViewModel()
            form.onTuningChange("Open D minor with capo 2")
            assertEquals("Open D minor with ca", form.state.value.tuning)
        }

    @Test
    fun noTuningSet_isStoredAsStandard() =
        runViewModelTest {
            val env = SongsTestEnv()
            val form = env.formViewModel()
            form.onTitleChange("Faro")
            form.onTuningChange("   ")
            form.save()
            assertNull(
                env.store.songs.value
                    .single()
                    .originalTuning,
            )
        }

    @Test
    fun editASong_loadsItsValuesAndBands() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.songs.save(Song(SongId("faro"), "Faro", "Mareas", "Am", 120, "Drop D", 225))
            env.songs.setBands(SongId("faro"), setOf(sabados.id))

            val form = env.formViewModel(SongId("faro"))
            val state = form.state.value

            assertFalse(state.isNew)
            assertEquals("Faro", state.title)
            assertEquals("120", state.bpm)
            assertEquals("3", state.minutes)
            assertEquals("45", state.seconds)
            assertEquals(setOf(sabados.id), state.selectedBands)
            assertEquals(listOf("Los Sábados", "Mareas"), state.bands.map { it.name })
        }

    @Test
    fun editingKeepsTheRepertoireOfArchivedBands() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("faro", "Faro", bands = setOf(mareas.id))
            env.bands.archive(mareas.id)

            val form = env.formViewModel(SongId("faro"))
            form.toggleBand(sabados.id)
            form.save()

            assertEquals(setOf(mareas.id, sabados.id), env.songs.observeBandsOf(SongId("faro")).first())
        }

    @Test
    fun deleteAnUnusedSong_removesItFromTheCatalogueAndRepertoires() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("faro", "Faro", bands = setOf(mareas.id))
            val form = env.formViewModel(SongId("faro"))

            form.delete()

            assertTrue(form.state.value.deleted)
            assertTrue(
                env.store.songs.value
                    .isEmpty(),
            )
            assertTrue(
                env.store.repertoire.value
                    .isEmpty(),
            )
        }

    @Test
    fun deleteASongUsedInSetlists_isRefusedAndListsThem() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            val faro = env.song("faro", "Faro")
            env.setlistUsing("Sala Norte", faro)
            env.setlistUsing("Fiestas", faro)
            val form = env.formViewModel(faro.id)

            form.delete()

            assertFalse(form.state.value.deleted)
            assertEquals(listOf("Fiestas", "Sala Norte"), form.state.value.blockingSetlists)
            assertEquals(1, env.store.songs.value.size)

            form.dismissBlocking()
            assertTrue(
                form.state.value.blockingSetlists
                    .isEmpty(),
            )
        }
}
