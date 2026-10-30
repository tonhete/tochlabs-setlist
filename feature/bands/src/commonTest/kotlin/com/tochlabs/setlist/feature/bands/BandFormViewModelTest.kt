package com.tochlabs.setlist.feature.bands

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// specs/bands: "Create and edit bands".
class BandFormViewModelTest {
    @Test
    fun createABand_appearsInTheBandListAndInEveryFilter() =
        runViewModelTest {
            val env = BandsTestEnv()
            val form = env.formViewModel()

            form.onNameChange("  Mareas ")
            form.onColorChange(bandPalette[2])
            form.save()

            assertTrue(form.state.value.saved)
            val active = env.bands.observeActive().first()
            assertEquals(listOf("Mareas"), active.map { it.name })
            assertEquals(bandPalette[2], active.single().color)
        }

    @Test
    fun rejectAnEmptyName() =
        runViewModelTest {
            val env = BandsTestEnv()
            val form = env.formViewModel()

            form.onNameChange("   ")
            form.save()

            assertTrue(form.state.value.nameError)
            assertFalse(form.state.value.saved)
            assertTrue(
                env.bands
                    .observeActive()
                    .first()
                    .isEmpty(),
            )
        }

    @Test
    fun typingAfterAnErrorClearsIt() =
        runViewModelTest {
            val form = BandsTestEnv().formViewModel()
            form.save()
            form.onNameChange("M")
            assertFalse(form.state.value.nameError)
        }

    @Test
    fun editABand_loadsItsValuesAndKeepsItsId() =
        runViewModelTest {
            val env = BandsTestEnv()
            val mareas = env.mareasWithData()
            val form = env.formViewModel(mareas.id)

            assertEquals("Mareas", form.state.value.name)
            assertEquals(mareas.color, form.state.value.color)
            assertFalse(form.state.value.isNew)

            form.onNameChange("Mareas Vivas")
            form.save()

            val saved = env.bands.observe(BandId("mareas")).first()
            assertEquals("Mareas Vivas", saved?.name)
            assertEquals(
                1,
                env.bands
                    .observeActive()
                    .first()
                    .size,
            )
        }

    @Test
    fun aNewBandStartsWithTheFirstPaletteColour() =
        runViewModelTest {
            val form = BandsTestEnv().formViewModel()
            assertTrue(form.state.value.isNew)
            assertEquals(bandPalette.first(), form.state.value.color)
        }
}
