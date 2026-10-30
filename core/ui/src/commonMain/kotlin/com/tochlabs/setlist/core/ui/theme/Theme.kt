package com.tochlabs.setlist.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF4F46B8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE3DFFF),
        onPrimaryContainer = Color(0xFF140A63),
        secondary = Color(0xFF5E5C71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE4E0F9),
        onSecondaryContainer = Color(0xFF1B1A2C),
        tertiary = Color(0xFF00687A),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFADECFF),
        onTertiaryContainer = Color(0xFF001F26),
        background = Color(0xFFFCF8FF),
        onBackground = Color(0xFF1C1B20),
        surface = Color(0xFFFCF8FF),
        onSurface = Color(0xFF1C1B20),
        surfaceVariant = Color(0xFFE5E1EC),
        onSurfaceVariant = Color(0xFF47464F),
        outline = Color(0xFF787680),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFC5C0FF),
        onPrimary = Color(0xFF261A84),
        primaryContainer = Color(0xFF3D339F),
        onPrimaryContainer = Color(0xFFE3DFFF),
        secondary = Color(0xFFC8C3DC),
        onSecondary = Color(0xFF302E41),
        secondaryContainer = Color(0xFF464559),
        onSecondaryContainer = Color(0xFFE4E0F9),
        tertiary = Color(0xFF83D2E6),
        onTertiary = Color(0xFF003640),
        tertiaryContainer = Color(0xFF004E5C),
        onTertiaryContainer = Color(0xFFADECFF),
        background = Color(0xFF131318),
        onBackground = Color(0xFFE5E1E9),
        surface = Color(0xFF131318),
        onSurface = Color(0xFFE5E1E9),
        surfaceVariant = Color(0xFF47464F),
        onSurfaceVariant = Color(0xFFC8C5D0),
        outline = Color(0xFF928F9A),
    )

/** App theme. Follows the system light or dark mode. */
@Composable
fun SetlistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
