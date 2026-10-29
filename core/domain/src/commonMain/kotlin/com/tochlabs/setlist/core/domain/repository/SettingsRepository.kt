package com.tochlabs.setlist.core.domain.repository

import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Stored settings, or the defaults when nothing was saved. */
    fun observe(): Flow<AppSettings>

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setDefaultPause(seconds: Int)
}
