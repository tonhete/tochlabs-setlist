package com.tochlabs.setlist.feature.setlists

import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/setlists: "Create and edit setlists"; specs/setlist-duration: "Configurable pause between songs".
class SetlistFormViewModelTest {
    @Test
    fun createASetlist_savedForTheBandWithItsDate() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val form = env.formViewModel()

            form.selectBand(mareas.id)
            form.onNameChange(" Concierto Sala Norte ")
            form.onDateChange(LocalDate(2026, 11, 20))
            form.save()

            val saved =
                env.store.setlists.value
                    .single()
            assertEquals(form.state.value.savedId, saved.id)
            assertEquals("Concierto Sala Norte", saved.name)
            assertEquals(mareas.id, saved.bandId)
            assertEquals(LocalDate(2026, 11, 20), saved.date)
        }

    @Test
    fun setlistWithoutDate_isSavedWithNoDate() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val form = env.formViewModel(bandId = sabados.id)

            form.onNameChange("Ensayo")
            form.save()

            val saved =
                env.store.setlists.value
                    .single()
            assertNull(saved.date)
            assertEquals(sabados.id, saved.bandId)
        }

    @Test
    fun newSetlistsStartWithTheDefaultPauseFromSettings() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.settings.setDefaultPause(30)
            val form = env.formViewModel(bandId = mareas.id)

            form.onNameChange("Sala")
            form.save()

            assertEquals(
                30,
                env.store.setlists.value
                    .single()
                    .pauseSeconds,
            )
        }

    @Test
    fun anEmptyNameIsRejected() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val form = env.formViewModel(bandId = mareas.id)

            form.onNameChange("   ")
            form.save()

            assertTrue(form.state.value.nameError)
            assertNull(form.state.value.savedId)
            assertTrue(
                env.store.setlists.value
                    .isEmpty(),
            )
        }

    @Test
    fun aBandIsRequired() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            val form = env.formViewModel()

            form.onNameChange("Sala")
            form.save()

            assertTrue(form.state.value.bandError)
            assertTrue(
                env.store.setlists.value
                    .isEmpty(),
            )
        }

    @Test
    fun withOneActiveBandItIsPreselected() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bands.save(mareas)
            val form = env.formViewModel()
            assertEquals(mareas.id, form.state.value.bandId)
        }

    @Test
    fun editASetlist_loadsItAndKeepsItsPause() =
        runViewModelTest {
            val env = SetlistsTestEnv()
            env.bothBands()
            env.setlists.save(Setlist(SetlistId("s"), mareas.id, "Sala", LocalDate(2026, 11, 20), pauseSeconds = 45))
            val form = env.formViewModel(SetlistId("s"))

            assertFalse(form.state.value.isNew)
            assertEquals("Sala", form.state.value.name)
            assertEquals(mareas.id, form.state.value.bandId)

            form.onNameChange("Sala Norte")
            form.onDateChange(null)
            form.save()

            assertNotNull(form.state.value.savedId)
            assertEquals(
                Setlist(SetlistId("s"), mareas.id, "Sala Norte", null, pauseSeconds = 45),
                env.store.setlists.value
                    .single(),
            )
        }
}
