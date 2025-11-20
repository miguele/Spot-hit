package com.spothit.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = DarkBackground,
    secondary = GreenSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = MutedOnDark,
    outline = OutlineDark,
    primaryContainer = GreenPrimary.copy(alpha = 0.18f),
    onPrimaryContainer = OnDark,
    secondaryContainer = GreenSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = GreenSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = DarkBackground,
    secondary = GreenSecondary,
    onSecondary = DarkBackground,
    background = OnDark,
    onBackground = DarkBackground,
    surface = OnDark,
    onSurface = DarkBackground,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkBackground,
    outline = OutlineDark,
    primaryContainer = GreenPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkBackground,
    secondaryContainer = GreenSecondary.copy(alpha = 0.25f),
    onSecondaryContainer = DarkBackground
)

@Composable
fun SpotHitTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = true

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpotHitTypography,
        content = content
    )
}
