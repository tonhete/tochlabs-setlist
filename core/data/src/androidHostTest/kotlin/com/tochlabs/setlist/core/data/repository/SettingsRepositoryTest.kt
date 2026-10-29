package com.tochlabs.setlist.core.data.repository

import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Scenarios from specs/app-settings and specs/setlist-duration (default pause).
internal class SettingsRepositoryTest : RepositoryTestBase() {
    private val settings = RoomSettingsRepository(db)

    @Test
    fun defaultsWhenNothingWasSaved() =
        runTest {
            assertEquals(AppSettings(AppLanguage.SYSTEM, defaultPauseSeconds = 20), settings.observe().first())
        }

    @Test
    fun switchToEnglishKeepsThePause() =
        runTest {
            settings.setDefaultPause(30)
            settings.setLanguage(AppLanguage.EN)
            assertEquals(AppSettings(AppLanguage.EN, defaultPauseSeconds = 30), settings.observe().first())
        }

    @Test
    fun changeTheDefaultKeepsTheLanguage() =
        runTest {
            settings.setLanguage(AppLanguage.ES)
            settings.setDefaultPause(30)
            assertEquals(AppSettings(AppLanguage.ES, defaultPauseSeconds = 30), settings.observe().first())
        }

    @Test
    fun existingSetlistsKeepTheirValue() =
        runTest {
            val mareas = band("Mareas")
            bands.save(mareas)
            setlists.save(setlist("Antigua", mareas))

            settings.setDefaultPause(45)

            assertEquals(20, setlists.observe(setlist("Antigua", mareas).id).first()?.pauseSeconds)
        }

    @Test
    fun invalidPauseIsRejected() =
        runTest {
            assertFailsWith<IllegalArgumentException> { settings.setDefaultPause(22) }
            assertFailsWith<IllegalArgumentException> { settings.setDefaultPause(125) }
        }
}
