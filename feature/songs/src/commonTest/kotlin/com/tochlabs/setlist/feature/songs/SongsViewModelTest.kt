package com.tochlabs.setlist.feature.songs

import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/song-catalog: catalogue list, "Repertoire per band", search, "Optional duration".
class SongsViewModelTest {
    @Test
    fun theCatalogueListsEverySongByTitleWithItsBands() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("2", "Faro", bands = setOf(mareas.id, sabados.id))
            env.song("1", "Arena")
            val vm = env.listViewModel()
            keepActive(vm.state)

            val rows = vm.state.value.songs
            assertEquals(listOf("Arena", "Faro"), rows.map { it.song.title })
            assertEquals(listOf("Los Sábados", "Mareas"), rows[1].bands.map { it.name })
            assertEquals(
                listOf("Los Sábados", "Mareas"),
                vm.state.value.bands
                    .map { it.name },
            )
            assertFalse(vm.state.value.loading)
        }

    @Test
    fun songSharedByTwoBands_appearsInBothRepertoires() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("1", "Faro", bands = setOf(mareas.id, sabados.id))
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.selectBand(mareas.id)
            assertEquals(
                listOf("Faro"),
                vm.state.value.songs
                    .map { it.song.title },
            )
            vm.selectBand(sabados.id)
            assertEquals(
                listOf("Faro"),
                vm.state.value.songs
                    .map { it.song.title },
            )
        }

    @Test
    fun filterSongsByBand_listsOnlyThatBandsRepertoire() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("1", "Faro", bands = setOf(mareas.id))
            env.song("2", "Domingo", bands = setOf(sabados.id))
            env.song("3", "Sin banda")
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.selectBand(sabados.id)

            assertEquals(sabados.id, vm.state.value.selectedBand)
            assertEquals(
                listOf("Domingo"),
                vm.state.value.songs
                    .map { it.song.title },
            )
        }

    @Test
    fun showAllBands_listsTheWholeCatalogue() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("1", "Faro", bands = setOf(mareas.id))
            env.song("2", "Sin banda")
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.selectBand(mareas.id)
            vm.selectBand(null)

            assertEquals(2, vm.state.value.songs.size)
        }

    @Test
    fun anArchivedBandIsNotOfferedAsFilter() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("1", "Faro", bands = setOf(mareas.id))
            val vm = env.listViewModel()
            keepActive(vm.state)
            vm.selectBand(mareas.id)

            env.bands.archive(mareas.id)

            assertEquals(
                listOf("Los Sábados"),
                vm.state.value.bands
                    .map { it.name },
            )
            assertNull(vm.state.value.selectedBand)
            assertEquals(
                listOf("Faro"),
                vm.state.value.songs
                    .map { it.song.title },
            )
        }

    @Test
    fun accentInsensitiveSearch() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.song("1", "Canción de cuna")
            env.song("2", "Faro", artist = "Mareas")
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.onQueryChange("cancion")

            assertEquals(
                listOf("Canción de cuna"),
                vm.state.value.songs
                    .map { it.song.title },
            )
        }

    @Test
    fun searchAlsoMatchesTheArtistAndRespectsTheBandFilter() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.bothBands()
            env.song("1", "Faro", artist = "Olas", bands = setOf(mareas.id))
            env.song("2", "Puerto", artist = "Olas", bands = setOf(sabados.id))
            val vm = env.listViewModel()
            keepActive(vm.state)

            vm.onQueryChange("olas")
            vm.selectBand(sabados.id)

            assertEquals(
                listOf("Puerto"),
                vm.state.value.songs
                    .map { it.song.title },
            )
        }

    @Test
    fun aSongWithoutDurationIsLabelledAsSuch() =
        runViewModelTest {
            val env = SongsTestEnv()
            env.song("1", "Faro", durationSeconds = 215)
            env.song("2", "Puerto")
            val vm = env.listViewModel()
            keepActive(vm.state)

            val rows =
                vm.state.value.songs
                    .associateBy { it.song.title }
            assertFalse(rows.getValue("Faro").missingDuration)
            assertTrue(rows.getValue("Puerto").missingDuration)
        }
}
