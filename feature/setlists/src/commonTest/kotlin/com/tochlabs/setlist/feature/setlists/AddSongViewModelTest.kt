package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlists: "Songs and stops as items", "Duplicate songs allowed with a warning".
// specs/song-catalog: "Search the own catalogue".
class AddSongViewModelTest {
    @Test
    fun addASong_isAppendedAsTheLastItem() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.addSong(setlist, env.song("Uno"))
            val dos = env.song("Dos")
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)

            vm.add(dos)

            val last = env.itemsOf(setlist).last() as SetlistItem.SongItem
            assertEquals(dos.id, last.songId)
            assertEquals(AddSongMessage("Dos", repeated = false), vm.state.value.message)
        }

    @Test
    fun addASongAlreadyPresent_isAddedAndTheUserIsWarned() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val uno = env.song("Uno")
            env.addSong(setlist, uno)
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)

            vm.add(uno)

            assertEquals(2, env.itemsOf(setlist).size)
            assertEquals(AddSongMessage("Uno", repeated = true), vm.state.value.message)

            vm.messageShown()
            assertNull(vm.state.value.message)
        }

    @Test
    fun accentInsensitiveSearch() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.song("Canción de cuna")
            env.song("Faro", artist = "Mareas")
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)

            vm.onQueryChange("cancion")
            assertEquals(
                listOf("Canción de cuna"),
                vm.state.value.results
                    .map { it.song.title },
            )

            vm.onQueryChange("mareas")
            assertEquals(
                listOf("Faro"),
                vm.state.value.results
                    .map { it.song.title },
            )
        }

    @Test
    fun songAlreadyInTheSetlist_isMarkedButCanStillBeAdded() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val uno = env.song("Uno")
            env.song("Dos")
            env.addSong(setlist, uno)
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)

            val present =
                vm.state.value.results
                    .associate { it.song.title to it.alreadyInSetlist }
            assertEquals(mapOf("Dos" to false, "Uno" to true), present)
        }

    @Test
    fun songNotFound_offersToCreateItWithTheTypedTitle() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            env.song("Uno")
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)
            assertFalse(vm.state.value.offerCreate)

            vm.onQueryChange(" Nueva canción ")
            assertTrue(
                vm.state.value.results
                    .isEmpty(),
            )
            assertTrue(vm.state.value.offerCreate)

            vm.createAndAdd()

            val created =
                env.store.songs.value
                    .single { it.title == "Nueva canción" }
            assertEquals(created.id, (env.itemsOf(setlist).single() as SetlistItem.SongItem).songId)
            assertEquals(AddSongMessage("Nueva canción", repeated = false), vm.state.value.message)
            assertEquals("", vm.state.value.query)
        }

    @Test
    fun blankQuery_doesNotOfferToCreate() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala Norte")
            val vm = env.addSongViewModel(setlist.id)
            keepActive(vm.state)

            vm.onQueryChange("   ")
            vm.createAndAdd()

            assertFalse(vm.state.value.offerCreate)
            assertTrue(
                env.store.songs.value
                    .isEmpty(),
            )
        }
}
