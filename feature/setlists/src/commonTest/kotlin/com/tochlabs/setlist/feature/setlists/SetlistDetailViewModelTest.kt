package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.domain.setlist.Change
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlists: items, reorder and remove, overrides, change markers, links, delete.
// specs/setlist-duration: totals, pause, songs without duration, toggle and breakdown.
class SetlistDetailViewModelTest {
    private fun SetlistDetailViewModel.titles() =
        state.value.rows.map { row ->
            when (val entry = row.entry) {
                is ResolvedItem.SongEntry -> entry.song.title
                is ResolvedItem.StopEntry -> entry.item.type.name
            }
        }

    private fun SetlistDetailViewModel.row(index: Int) = state.value.rows[index]

    @Test
    fun showsTheSetlistWithBandItemsAndSongNumbers() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            env.addStop(setlist, StopType.SPEECH, 60)
            env.addSong(setlist, env.song("Dos"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            assertEquals(setlist, vm.state.value.setlist)
            assertEquals(mareas, vm.state.value.band)
            assertEquals(listOf("Uno", "SPEECH", "Dos"), vm.titles())
            assertEquals(
                listOf(1, null, 2),
                vm.state.value.rows
                    .map { it.songNumber },
            )
            assertFalse(vm.state.value.loading)
        }

    @Test
    fun addAStop_breakOf15MinutesIsAppendedAsTheLastItem() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.addStop(StopDraft().withType(StopType.BREAK).withDuration(minutes(15)))

            val last = env.itemsOf(setlist).last() as SetlistItem.Stop
            assertEquals(StopType.BREAK, last.type)
            assertEquals(minutes(15), last.durationSeconds)
            assertEquals(listOf("Uno", "BREAK"), vm.titles())
        }

    @Test
    fun editAStop_changesTypeDurationAndNote() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addStop(setlist, StopType.SPEECH, 60)
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            val draft = vm.row(0).stopDraft()!!
            vm.saveStop(draft.withType(StopType.ENCORE).adjust(30).withNote(" Bis "))

            val stop = env.itemsOf(setlist).single() as SetlistItem.Stop
            assertEquals(StopType.ENCORE, stop.type)
            assertEquals(90, stop.durationSeconds)
            assertEquals("Bis", stop.note)
        }

    @Test
    fun reorderByDragging_thirdItemToFirstPosition() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val uno = env.addSong(setlist, env.song("Uno"))
            env.addSong(setlist, env.song("Dos"))
            val tres = env.addSong(setlist, env.song("Tres"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.onDragMove(tres.id, uno.id)
            // While dragging the list shows the new order before it is saved.
            assertEquals(listOf("Tres", "Uno", "Dos"), vm.titles())

            vm.onDragEnd()

            assertEquals(listOf("Tres", "Uno", "Dos"), vm.titles())
            assertEquals(
                listOf(1, 2, 3),
                vm.state.value.rows
                    .map { it.songNumber },
            )
            assertEquals(
                listOf("Tres", "Uno", "Dos"),
                env.itemsOf(setlist).map { (it as SetlistItem.SongItem).songId.value },
            )
        }

    @Test
    fun moveUpAndDownButtons() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val uno = env.addSong(setlist, env.song("Uno"))
            val dos = env.addSong(setlist, env.song("Dos"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)
            assertFalse(vm.row(0).canMoveUp)
            assertFalse(vm.row(1).canMoveDown)

            vm.moveDown(uno.id)
            assertEquals(listOf("Dos", "Uno"), vm.titles())

            vm.moveUp(uno.id)
            assertEquals(listOf("Uno", "Dos"), vm.titles())

            // At the edges nothing changes.
            vm.moveUp(uno.id)
            vm.moveDown(dos.id)
            assertEquals(listOf("Uno", "Dos"), vm.titles())
        }

    @Test
    fun removeASongFromASetlist_theSongStaysInTheCatalogue() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val item = env.addSong(setlist, env.song("Uno"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.removeItem(item.id)

            assertTrue(
                vm.state.value.rows
                    .isEmpty(),
            )
            assertEquals(
                listOf("Uno"),
                env.store.songs.value
                    .map { it.title },
            )
        }

    @Test
    fun overrideTheKey_itemShowsTheNewKeyWithTheOriginalAsReference() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno", key = "A"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            val draft = vm.row(0).songDraft()!!
            assertEquals("A", draft.originalKey)
            vm.saveSongItem(draft.withKey("G"))

            assertEquals(Change<String?>("A", "G"), vm.row(0).markers?.keyChange)
            assertEquals("G", (env.itemsOf(setlist).single() as SetlistItem.SongItem).keyOverride)
            assertEquals(
                "A",
                env.store.songs.value
                    .single()
                    .originalKey,
            )
        }

    @Test
    fun instrumentAndTuningLimits_onlyTheFirst20CharactersAreKept() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            val draft =
                vm
                    .row(0)
                    .songDraft()!!
                    .withInstrument("Guitarra acústica de doce cuerdas")
                    .withTuning("Open D minor with capo 2")
            assertEquals("Guitarra acústica de", draft.instrument)
            assertEquals("Open D minor with ca", draft.tuning)
            vm.saveSongItem(draft)

            val item = env.itemsOf(setlist).single() as SetlistItem.SongItem
            assertEquals("Guitarra acústica de", item.instrument)
            assertEquals("Open D minor with ca", item.tuningOverride)
        }

    @Test
    fun blankOverridesAreStoredAsNoOverride() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno", key = "A", bpm = 100))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.saveSongItem(
                vm
                    .row(0)
                    .songDraft()!!
                    .withKey(" ")
                    .withBpm("")
                    .withNote(" "),
            )

            val item = env.itemsOf(setlist).single() as SetlistItem.SongItem
            assertNull(item.keyOverride)
            assertNull(item.bpmOverride)
            assertNull(item.note)
        }

    @Test
    fun instrumentChange_marksTheSongThatChangesInstrument() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"), instrument = "Strato")
            env.addSong(setlist, env.song("Dos"), instrument = "Strato")
            env.addSong(setlist, env.song("Tres"), instrument = "Acústica")
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            assertNull(vm.row(1).markers?.instrumentChangeTo)
            assertEquals("Acústica", vm.row(2).markers?.instrumentChangeTo)
        }

    @Test
    fun linkTwoConsecutiveSongs_bothAreShownLinkedWithTheText() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            env.addSong(setlist, env.song("Dos"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            val draft = vm.row(0).songDraft()!!
            assertTrue(draft.nextIsSong)
            vm.saveSongItem(draft.withLinked(true).withTransition("End on A and drums come in"))

            assertTrue(vm.row(0).markers!!.isLinkedToNext)
            assertTrue(vm.row(1).markers!!.isLinkedFromPrevious)
            assertEquals("End on A and drums come in", vm.row(0).markers?.transitionText)
        }

    @Test
    fun linkBrokenByAStop_hasNoEffectUntilTheSongsAreConsecutiveAgain() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"), linkedToNext = true)
            val dos = env.addSong(setlist, env.song("Dos"))
            val stop = env.addStop(setlist, StopType.SPEECH, 60)
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.moveUp(stop.id)
            assertFalse(vm.row(0).markers!!.isLinkedToNext)
            assertFalse(vm.row(0).songDraft()!!.nextIsSong)

            vm.moveDown(stop.id)
            assertTrue(vm.row(0).markers!!.isLinkedToNext)
            assertEquals(dos.id, (vm.row(1).entry as ResolvedItem.SongEntry).item.id)
        }

    @Test
    fun repeatedSong_secondOccurrenceIsMarked() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val uno = env.song("Uno")
            env.addSong(setlist, uno)
            env.addSong(setlist, uno)
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            assertFalse(vm.row(0).markers!!.isRepeated)
            assertTrue(vm.row(1).markers!!.isRepeated)
        }

    @Test
    fun totalsMatchTheDomainRules_songsStopAndPauses() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte", pauseSeconds = 20)
            env.addSong(setlist, env.song("Uno", minutes(3)))
            env.addSong(setlist, env.song("Dos", minutes(4)))
            env.addStop(setlist, StopType.SPEECH, minutes(1))
            env.addSong(setlist, env.song("Tres", minutes(5)))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            val duration = vm.state.value.duration
            assertEquals(minutes(12).toLong(), duration.musicSeconds)
            assertEquals(3, duration.pauseCount)
            assertEquals(minutes(14).toLong(), duration.totalWithStopsSeconds)
            assertEquals(TotalMode.WITH_STOPS, vm.state.value.totalMode)
            assertEquals(minutes(14).toLong(), vm.state.value.shownTotalSeconds)
        }

    @Test
    fun switchToMusicOnly_excludesStopsAndPausesAndSaysWhatWasLeftOut() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte", pauseSeconds = 20)
            env.addSong(setlist, env.song("Uno", minutes(3)))
            env.addStop(setlist, StopType.SPEECH, minutes(1))
            env.addSong(setlist, env.song("Dos", minutes(4)))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.setTotalMode(TotalMode.MUSIC_ONLY)

            assertEquals(minutes(7).toLong(), vm.state.value.shownTotalSeconds)
            assertEquals(minutes(1, 40).toLong(), vm.state.value.duration.leftOutOfMusicOnlySeconds)
        }

    @Test
    fun changeThePause_totalIsRecalculated() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte", pauseSeconds = 20)
            env.addSong(setlist, env.song("Uno", minutes(3)))
            env.addSong(setlist, env.song("Dos", minutes(4)))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.increasePause()

            assertEquals(
                25,
                env.store.setlists.value
                    .single()
                    .pauseSeconds,
            )
            assertEquals(minutes(7, 25).toLong(), vm.state.value.duration.totalWithStopsSeconds)
        }

    @Test
    fun pauseLimits_valueDoesNotChangeBeyond0Or120() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val zero = env.setlist("Cero", pauseSeconds = 0)
            val max = env.setlist("Max", pauseSeconds = 120)
            val vmZero = env.detailViewModel(zero.id)
            val vmMax = env.detailViewModel(max.id)
            keepActive(vmZero.state)
            keepActive(vmMax.state)
            assertFalse(vmZero.state.value.canDecreasePause)
            assertFalse(vmMax.state.value.canIncreasePause)

            vmZero.decreasePause()
            vmMax.increasePause()

            val pauses =
                env.store.setlists.value
                    .associate { it.name to it.pauseSeconds }
            assertEquals(0, pauses["Cero"])
            assertEquals(120, pauses["Max"])
        }

    @Test
    fun oneSongWithoutDuration_totalIsIncomplete() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno", minutes(3)))
            env.addSong(setlist, env.song("Dos", minutes(4)))
            env.addSong(setlist, env.song("Tres"))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            assertEquals(1, vm.state.value.duration.songsWithoutDuration)
            assertFalse(vm.state.value.duration.isComplete)
            assertEquals(minutes(7).toLong(), vm.state.value.duration.musicSeconds)
        }

    @Test
    fun fasterTempoOverride_doesNotChangeTheDuration() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno", minutes(3), bpm = 100))
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.saveSongItem(vm.row(0).songDraft()!!.withBpm("140"))

            assertEquals(Change<Int?>(100, 140), vm.row(0).markers?.tempoChange)
            assertEquals(minutes(3).toLong(), vm.state.value.duration.musicSeconds)
        }

    @Test
    fun confirmedDeletion_removesTheSetlistKeepsSongsAndTheEvent() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            env.linkEvent(setlist)
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)
            assertEquals(
                "Gig",
                vm.state.value.event
                    ?.title,
            )

            vm.delete()

            assertTrue(vm.state.value.deleted)
            assertTrue(
                env.store.setlists.value
                    .isEmpty(),
            )
            assertEquals(1, env.store.songs.value.size)
            val event =
                env.store.events.value
                    .single()
            assertNull(event.setlistId)
        }

    @Test
    fun reorderModeToggles() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val vm = env.detailViewModel(setlist.id)
            keepActive(vm.state)

            vm.toggleReorderMode()
            assertTrue(vm.state.value.reorderMode)
            vm.toggleReorderMode()
            assertFalse(vm.state.value.reorderMode)
        }
}
