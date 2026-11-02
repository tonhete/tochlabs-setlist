package com.tochlabs.setlist.feature.stage

import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.testing.FakeBandRepository
import com.tochlabs.setlist.core.testing.FakeSetlistRepository
import com.tochlabs.setlist.core.testing.FakeSongRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
private val setlist = Setlist(SetlistId("sala"), mareas.id, "Sala Norte", pauseSeconds = 20)

/** One setlist in a fake database; items are added in order. */
private class StageTestEnv {
    val store = FakeStore()
    val setlists = FakeSetlistRepository(store)
    val songs = FakeSongRepository(store)
    private var next = 1

    suspend fun setUp() {
        FakeBandRepository(store).save(mareas)
        setlists.save(setlist)
    }

    suspend fun song(
        title: String,
        durationSeconds: Int? = 180,
        key: String? = null,
        instrument: String? = null,
        note: String? = null,
        keyOverride: String? = null,
        linkedToNext: Boolean = false,
        transitionText: String? = null,
    ) {
        val song = Song(SongId(title), title, artist = "Olas", originalKey = key, durationSeconds = durationSeconds)
        songs.save(song)
        setlists.addItem(
            setlist.id,
            SetlistItem.SongItem(
                SetlistItemId("item-${next++}"),
                song.id,
                keyOverride = keyOverride,
                instrument = instrument,
                note = note,
                linkedToNext = linkedToNext,
                transitionText = transitionText,
            ),
        )
    }

    suspend fun stop(
        type: StopType,
        durationSeconds: Int,
        note: String? = null,
    ) = setlists.addItem(setlist.id, SetlistItem.Stop(SetlistItemId("item-${next++}"), type, durationSeconds, note))

    fun viewModel() = StageViewModel(setlist.id, setlists, songs)
}

private fun StageItem.title(): String =
    when (val entry = entry) {
        is ResolvedItem.SongEntry -> entry.song.title
        is ResolvedItem.StopEntry -> entry.item.type.name
    }

// specs/stage-mode.
class StageViewModelTest {
    @Test
    fun openASetlist_showsTheFirstItemWithItsPosition() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            repeat(9) { env.song("Song ${it + 1}") }
            val vm = env.viewModel()
            keepActive(vm.state)

            val state = vm.state.value
            assertEquals("Sala Norte", state.setlistName)
            assertEquals("Song 1", state.current?.title())
            assertEquals(1, state.position)
            assertEquals(9, state.total)
            assertFalse(state.empty)
        }

    @Test
    fun emptySetlist_isReportedAsEmpty() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            val vm = env.viewModel()
            keepActive(vm.state)

            assertTrue(vm.state.value.empty)
            assertNull(vm.state.value.current)
        }

    @Test
    fun songWithChangesAndANote_showsTheInstrumentChangeAndTheNote() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno", instrument = "Strato")
            env.song("Dos", instrument = "Acústica", note = "Capo 2")
            val vm = env.viewModel()
            keepActive(vm.state)

            vm.next()

            val current = vm.state.value.current!!
            assertEquals("Acústica", current.markers?.instrumentChangeTo)
            assertEquals("Capo 2", current.note)
            assertEquals(2, current.songNumber)
        }

    @Test
    fun songsShowTheValuesOfTheSetlist() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno", key = "A", keyOverride = "G")
            val vm = env.viewModel()
            keepActive(vm.state)

            assertEquals(
                "G",
                vm.state.value.current
                    ?.key,
            )
        }

    @Test
    fun linkedSong_showsTheLinkAndItsText() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno", linkedToNext = true, transitionText = "End on E")
            env.song("Dos")
            val vm = env.viewModel()
            keepActive(vm.state)

            val markers =
                vm.state.value.current
                    ?.markers!!
            assertTrue(markers.isLinkedToNext)
            assertEquals("End on E", markers.transitionText)
        }

    @Test
    fun missingDuration_isShownAsNone() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno", durationSeconds = null)
            val vm = env.viewModel()
            keepActive(vm.state)

            assertNull(
                vm.state.value.current
                    ?.durationSeconds,
            )
        }

    @Test
    fun breakWithANote_showsTypeDurationAndNote() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.stop(StopType.BREAK, 15 * 60, "Drinks and merch")
            val vm = env.viewModel()
            keepActive(vm.state)

            val current = vm.state.value.current!!
            val stop = (current.entry as ResolvedItem.StopEntry).item
            assertEquals(StopType.BREAK, stop.type)
            assertEquals(15 * 60, current.durationSeconds)
            assertEquals("Drinks and merch", current.note)
            assertNull(current.songNumber)
        }

    @Test
    fun nextItem_fromTheThirdShowsTheFourthWithThePreviewOfTheNext() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno")
            env.song("Dos")
            env.song("Tres")
            env.stop(StopType.SPEECH, 60)
            env.song("Cinco")
            val vm = env.viewModel()
            keepActive(vm.state)
            vm.goTo(2)
            assertEquals(
                "SPEECH",
                vm.state.value.nextItem
                    ?.title(),
            )

            vm.next()

            assertEquals(
                "SPEECH",
                vm.state.value.current
                    ?.title(),
            )
            assertEquals(4, vm.state.value.position)
            assertEquals(
                "Cinco",
                vm.state.value.nextItem
                    ?.title(),
            )
        }

    @Test
    fun limits_theCurrentItemStaysAndTheControlIsDisabled() =
        runViewModelTest {
            val env = StageTestEnv()
            env.setUp()
            env.song("Uno")
            env.song("Dos")
            val vm = env.viewModel()
            keepActive(vm.state)

            assertFalse(vm.state.value.canGoBack)
            vm.previous()
            assertEquals(1, vm.state.value.position)

            vm.next()
            assertFalse(vm.state.value.canGoForward)
            assertNull(vm.state.value.nextItem)
            vm.next()
            assertEquals(2, vm.state.value.position)
            vm.goTo(5)
            assertEquals(2, vm.state.value.position)
        }
}
