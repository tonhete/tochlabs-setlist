package com.tochlabs.setlist.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

// Runtime language override for iOS, as in the Compose Multiplatform documentation: the choice is
// written to AppleLanguages and the UI is recomposed under a key. To be verified on a simulator in
// task 12.2 (design.md, decision 9).

private const val LANGUAGES_KEY = "AppleLanguages"

/** Captured at start-up, before any override. */
private val deviceTag: String = NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"

private val LocalIosLocale = staticCompositionLocalOf { deviceTag }

internal actual fun deviceLanguage(): String = deviceTag.substringBefore('-')

@Composable
internal actual fun ProvideAppLocale(
    tag: String?,
    content: @Composable () -> Unit,
) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (tag == null) defaults.removeObjectForKey(LANGUAGES_KEY) else defaults.setObject(listOf(tag), LANGUAGES_KEY)
    val applied = tag ?: deviceTag
    CompositionLocalProvider(LocalIosLocale provides applied) {
        key(applied) { content() }
    }
}
