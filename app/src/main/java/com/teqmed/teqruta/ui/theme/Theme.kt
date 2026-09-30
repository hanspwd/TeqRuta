package com.teqmed.teqruta.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blurple,
    secondary = Blurple,
    tertiary = Blurple,
    background = DiscordDarkBg,
    surface = DiscordDarkSurface,
    surfaceVariant = DiscordDarkSurfaceVariant,
    onBackground = DiscordText,
    onSurface = DiscordText,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = Blurple,
    secondary = Blurple,
    tertiary = Blurple,
    background = DiscordLightBg,
    surface = DiscordLightSurface,
    surfaceVariant = DiscordLightSurfaceVariant,
    onBackground = DiscordLightText,
    onSurface = DiscordLightText,
    error = ErrorRed
)

@Composable
fun TeqRutaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}