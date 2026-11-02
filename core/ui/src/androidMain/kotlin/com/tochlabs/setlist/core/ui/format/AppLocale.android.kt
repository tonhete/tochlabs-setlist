package com.tochlabs.setlist.core.ui.format

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

// Runtime language override for Android, after the Compose Multiplatform documentation ("change the
// locale at runtime"), with one change: a new Configuration object is provided instead of keying the
// whole UI on the locale. Readers of LocalConfiguration (string resources, Material pickers) update,
// while navigation and screen state are kept.

/** Captured before the first override, so "follow the device" can restore it. */
private val deviceLocale: Locale = Locale.getDefault()

internal actual fun deviceLanguage(): String = deviceLocale.language

@Composable
internal actual fun ProvideAppLocale(
    tag: String?,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val base = LocalConfiguration.current
    val locale = tag?.let(Locale::forLanguageTag) ?: deviceLocale
    val configuration = remember(base, locale) { Configuration(base).apply { setLocale(locale) } }
    // Code outside composition reads the default locale: resource lookups in view models
    // (PDF texts) and the formats that use the platform locale.
    if (Locale.getDefault() != locale) Locale.setDefault(locale)
    // Material components read their own strings from the activity resources.
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    CompositionLocalProvider(LocalConfiguration provides configuration) { content() }
}
