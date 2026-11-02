package com.tochlabs.setlist.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.domain.validation.PauseRules
import com.tochlabs.setlist.core.domain.validation.decreasePause
import com.tochlabs.setlist.core.domain.validation.increasePause
import com.tochlabs.setlist.core.model.AppLanguage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Values that come from the build, provided by the platform. */
data class AppInfo(
    val version: String,
)

/** The about section (specs/app-settings, "About"). */
data class About(
    val version: String,
    val licence: String = LICENCE,
    val repositoryUrl: String = REPOSITORY_URL,
)

data class SettingsUiState(
    val loading: Boolean = true,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val defaultPauseSeconds: Int = PauseRules.DEFAULT_SECONDS,
    val about: About = About(version = ""),
) {
    val canDecreasePause: Boolean get() = defaultPauseSeconds > PauseRules.MIN_SECONDS
    val canIncreasePause: Boolean get() = defaultPauseSeconds < PauseRules.MAX_SECONDS
}

/** Language, default pause for new setlists, and information about the app (specs/app-settings). */
class SettingsViewModel(
    private val settings: SettingsRepository,
    info: AppInfo,
) : ViewModel() {
    private val about = About(version = info.version)

    val state: StateFlow<SettingsUiState> =
        settings
            .observe()
            .map {
                SettingsUiState(
                    loading = false,
                    language = it.language,
                    defaultPauseSeconds = it.defaultPauseSeconds,
                    about = about,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                SettingsUiState(about = about),
            )

    /** Applied at once: the app root follows the stored language. */
    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch { settings.setLanguage(language) }
    }

    fun increaseDefaultPause() = updatePause(::increasePause)

    fun decreaseDefaultPause() = updatePause(::decreasePause)

    /** Only new setlists use the default; existing ones keep their own pause. */
    private fun updatePause(change: (Int) -> Int) {
        val current = state.value.defaultPauseSeconds
        val next = change(current)
        if (next != current) viewModelScope.launch { settings.setDefaultPause(next) }
    }
}

const val LICENCE = "GPL-3.0"
const val REPOSITORY_URL = "https://github.com/tonhete/tochlabs-setlist"
private const val STOP_TIMEOUT_MILLIS = 5_000L
