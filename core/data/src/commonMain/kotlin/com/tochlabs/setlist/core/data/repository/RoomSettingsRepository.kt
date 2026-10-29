package com.tochlabs.setlist.core.data.repository

import androidx.room3.withWriteTransaction
import com.tochlabs.setlist.core.data.db.AppSettingsEntity
import com.tochlabs.setlist.core.data.db.SetlistDatabase
import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.domain.validation.PauseRules
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.validatePause
import com.tochlabs.setlist.core.model.AppLanguage
import com.tochlabs.setlist.core.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Single-row table (design.md, decision 3). No row yet means the defaults. */
internal class RoomSettingsRepository(
    private val db: SetlistDatabase,
) : SettingsRepository {
    private val dao = db.settingsDao()

    override fun observe(): Flow<AppSettings> =
        dao.observe().map { row ->
            AppSettings(
                language = row?.language ?: DEFAULTS.language,
                defaultPauseSeconds = row?.defaultPauseSeconds ?: DEFAULTS.defaultPauseSeconds,
            )
        }

    override suspend fun setLanguage(language: AppLanguage) = write { it.copy(language = language) }

    /** The caller validates user input; an invalid value here is a programming error. */
    override suspend fun setDefaultPause(seconds: Int) {
        require(validatePause(seconds) is Validated.Valid) { "Invalid pause: $seconds" }
        write { it.copy(defaultPauseSeconds = seconds) }
    }

    private suspend fun write(change: (AppSettingsEntity) -> AppSettingsEntity) {
        db.withWriteTransaction {
            val existing = dao.get()
            val current =
                existing
                    ?: AppSettingsEntity(
                        language = DEFAULTS.language,
                        defaultPauseSeconds = DEFAULTS.defaultPauseSeconds,
                    )
            if (existing == null) dao.insert(change(current)) else dao.update(change(current))
        }
    }

    private companion object {
        val DEFAULTS = AppSettings(AppLanguage.SYSTEM, PauseRules.DEFAULT_SECONDS)
    }
}
