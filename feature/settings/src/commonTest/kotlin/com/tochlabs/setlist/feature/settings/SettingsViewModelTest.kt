package com.tochlabs.setlist.feature.settings

import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.AppSettings
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.testing.FakeSetlistRepository
import com.tochlabs.setlist.core.testing.FakeSettingsRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.core.testing.keepActive
import com.tochlabs.setlist.core.testing.runViewModelTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// specs/app-settings.
class SettingsViewModelTest {
    private val info = AppInfo(version = "1.0")

    @Test
    fun showsTheStoredLanguageAndDefaultPause() =
        runViewModelTest {
            val settings = FakeSettingsRepository(AppSettings(AppLanguage.ES, 25))
            val vm = SettingsViewModel(settings, info)
            keepActive(vm.state)

            assertEquals(AppLanguage.ES, vm.state.value.language)
            assertEquals(25, vm.state.value.defaultPauseSeconds)
            assertFalse(vm.state.value.loading)
        }

    @Test
    fun switchToEnglish_theChoiceIsStored() =
        runViewModelTest {
            val settings = FakeSettingsRepository()
            val vm = SettingsViewModel(settings, info)
            keepActive(vm.state)
            assertEquals(AppLanguage.SYSTEM, vm.state.value.language, "follows the device until the user chooses")

            vm.selectLanguage(AppLanguage.EN)

            assertEquals(AppLanguage.EN, settings.observe().first().language)
            assertEquals(AppLanguage.EN, vm.state.value.language)
        }

    @Test
    fun changeTheDefaultPause_inStepsOf5() =
        runViewModelTest {
            val settings = FakeSettingsRepository()
            val vm = SettingsViewModel(settings, info)
            keepActive(vm.state)

            vm.increaseDefaultPause()
            vm.increaseDefaultPause()

            assertEquals(30, settings.observe().first().defaultPauseSeconds)
        }

    @Test
    fun defaultPauseLimits_valueDoesNotChangeBeyond0Or120() =
        runViewModelTest {
            val atZero = FakeSettingsRepository(AppSettings(AppLanguage.SYSTEM, 0))
            val vmZero = SettingsViewModel(atZero, info)
            keepActive(vmZero.state)
            assertFalse(vmZero.state.value.canDecreasePause)
            vmZero.decreaseDefaultPause()
            assertEquals(0, atZero.observe().first().defaultPauseSeconds)

            val atMax = FakeSettingsRepository(AppSettings(AppLanguage.SYSTEM, 120))
            val vmMax = SettingsViewModel(atMax, info)
            keepActive(vmMax.state)
            assertFalse(vmMax.state.value.canIncreasePause)
            vmMax.increaseDefaultPause()
            assertEquals(120, atMax.observe().first().defaultPauseSeconds)
        }

    @Test
    fun existingSetlistsKeepTheirPause() =
        runViewModelTest {
            val store = FakeStore()
            val setlists = FakeSetlistRepository(store)
            val earlier = Setlist(SetlistId("s"), BandId("b"), "Sala", pauseSeconds = 20)
            setlists.save(earlier)
            val vm = SettingsViewModel(FakeSettingsRepository(), info)
            keepActive(vm.state)

            vm.increaseDefaultPause()
            vm.increaseDefaultPause()

            assertEquals(listOf(earlier), store.setlists.value)
        }

    @Test
    fun aboutShowsVersionLicenceAndRepository() =
        runViewModelTest {
            val vm = SettingsViewModel(FakeSettingsRepository(), AppInfo(version = "1.2"))
            keepActive(vm.state)

            val about = vm.state.value.about
            assertEquals("1.2", about.version)
            assertEquals("GPL-3.0", about.licence)
            assertTrue(about.repositoryUrl.startsWith("https://github.com/"))
        }
}
