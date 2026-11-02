package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.domain.validation.PauseRules
import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Starts with the app defaults: system language, 20 second pause. */
class FakeSettingsRepository(
    initial: AppSettings = AppSettings(AppLanguage.SYSTEM, PauseRules.DEFAULT_SECONDS),
) : SettingsRepository {
    private val settings = MutableStateFlow(initial)

    override fun observe(): Flow<AppSettings> = settings

    override suspend fun setLanguage(language: AppLanguage) = settings.update { it.copy(language = language) }

    override suspend fun setDefaultPause(seconds: Int) = settings.update { it.copy(defaultPauseSeconds = seconds) }
}
