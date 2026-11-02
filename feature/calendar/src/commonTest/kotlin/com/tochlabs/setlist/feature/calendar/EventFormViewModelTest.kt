package com.tochlabs.setlist.feature.calendar

import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// specs/event-calendar: "Create and edit events".
class EventFormViewModelTest {
    @Test
    fun createAGig_isSavedWithAllItsData() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val form = env.formViewModel()

            form.selectBand(mareas.id)
            form.onDateChange(LocalDate(2026, 11, 20))
            form.onTitleChange(" Concierto Sala Norte ")
            form.onVenueChange("Sala Norte")
            form.onAddressChange("Calle Mayor 1")
            form.onParkingChange("Loading bay at the back")
            form.onContactChange("Ana 600 000 000")
            form.save()

            assertEquals(
                Event(
                    EventId("id-1"),
                    mareas.id,
                    EventType.GIG,
                    LocalDate(2026, 11, 20),
                    "Concierto Sala Norte",
                    venue = "Sala Norte",
                    address = "Calle Mayor 1",
                    parking = "Loading bay at the back",
                    contact = "Ana 600 000 000",
                ),
                env.store.events.value
                    .single(),
            )
            assertEquals(EventId("id-1"), form.state.value.savedId)
        }

    @Test
    fun createARehearsal_withPreselectedDateAndBand() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val form = env.formViewModel(date = LocalDate(2026, 11, 18), bandId = sabados.id)

            form.selectType(EventType.REHEARSAL)
            form.onTitleChange("Ensayo")
            form.save()

            val saved =
                env.store.events.value
                    .single()
            assertEquals(EventType.REHEARSAL, saved.type)
            assertEquals(LocalDate(2026, 11, 18), saved.date)
            assertEquals(sabados.id, saved.bandId)
            assertNull(saved.venue)
        }

    @Test
    fun missingDate_isNotSavedAndTheDateIsRequired() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val form = env.formViewModel(bandId = mareas.id)

            form.onTitleChange("Concierto")
            form.save()

            assertTrue(form.state.value.dateError)
            assertNull(form.state.value.savedId)
            assertTrue(
                env.store.events.value
                    .isEmpty(),
            )
        }

    @Test
    fun missingTitleOrBand_isNotSaved() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val form = env.formViewModel(date = TODAY)

            form.onTitleChange("  ")
            form.save()

            assertTrue(form.state.value.titleError)
            assertTrue(form.state.value.bandError)
            assertTrue(
                env.store.events.value
                    .isEmpty(),
            )
        }

    @Test
    fun withOneActiveBandItIsPreselected() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bands.save(mareas)
            assertEquals(
                mareas.id,
                env
                    .formViewModel()
                    .state.value.bandId,
            )
        }

    @Test
    fun editAnEvent_loadsItsValuesAndKeepsItsSetlist() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala")
            val event = env.event("Concierto", LocalDate(2026, 11, 20), setlistId = setlist.id, parking = "Back")
            val form = env.formViewModel(event.id)

            assertFalse(form.state.value.isNew)
            assertEquals("Concierto", form.state.value.title)
            assertEquals("Back", form.state.value.parking)

            form.onTitleChange("Concierto grande")
            form.save()

            assertEquals(
                event.copy(title = "Concierto grande"),
                env.store.events.value
                    .single(),
            )
        }

    @Test
    fun changingTheBand_unlinksTheSetlistOfTheOtherBand() =
        runViewModelTest {
            val env = CalendarTestEnv()
            env.bothBands()
            val setlist = env.setlist("Sala")
            val event = env.event("Concierto", LocalDate(2026, 11, 20), setlistId = setlist.id)
            val form = env.formViewModel(event.id)

            form.selectBand(sabados.id)
            form.save()

            val saved =
                env.store.events.value
                    .single()
            assertEquals(sabados.id, saved.bandId)
            assertNull(saved.setlistId)
        }
}
