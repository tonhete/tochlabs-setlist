package com.tochlabs.setlist.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.tochlabs.setlist.core.model.AppLanguage

/**
 * Language the interface is shown in: the user's choice, or the device language when it is
 * Spanish, or English otherwise (specs/localization, "Spanish and English interface").
 */
fun resolveUiLanguage(
    choice: AppLanguage,
    deviceLanguage: String,
): UiLanguage =
    when (choice) {
        AppLanguage.ES -> UiLanguage.ES
        AppLanguage.EN -> UiLanguage.EN
        AppLanguage.SYSTEM -> if (deviceLanguage == "es") UiLanguage.ES else UiLanguage.EN
    }

/** The applied interface language; read it with [currentUiLanguage]. */
internal val LocalUiLanguage = staticCompositionLocalOf { resolveUiLanguage(AppLanguage.SYSTEM, deviceLanguage()) }

/**
 * Applies the chosen language to everything inside: string resources, formats and the platform's
 * own components (date and time pickers). Changing it takes effect at once (specs/app-settings).
 */
@Composable
fun AppLanguageEnvironment(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val tag =
        when (language) {
            AppLanguage.SYSTEM -> null
            AppLanguage.ES -> "es"
            AppLanguage.EN -> "en"
        }
    ProvideAppLocale(tag) {
        CompositionLocalProvider(LocalUiLanguage provides resolveUiLanguage(language, deviceLanguage())) {
            content()
        }
    }
}

/** Language of the device, as it was when the app started (before any in-app choice). */
internal expect fun deviceLanguage(): String

/**
 * Overrides the locale used by resources for [content]; null restores the device locale
 * (design.md, decision 9: LocaleController).
 */
@Composable
internal expect fun ProvideAppLocale(
    tag: String?,
    content: @Composable () -> Unit,
)
