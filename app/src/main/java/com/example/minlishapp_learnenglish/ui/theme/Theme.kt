package com.example.minlishapp_learnenglish.ui.theme

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
    primary = MinLishPrimaryFixedDim,
    onPrimary = MinLishOnPrimary,
    primaryContainer = MinLishPrimary,
    onPrimaryContainer = MinLishOnPrimary,
    secondary = MinLishSecondaryContainer,
    onSecondary = MinLishOnSecondaryContainer,
    secondaryContainer = MinLishSecondary,
    onSecondaryContainer = MinLishOnSecondary,
    tertiary = MinLishTertiaryFixedDim,
    onTertiary = MinLishOnTertiaryFixed,
    background = MinLishOnSurface,
    onBackground = MinLishSurface,
    surface = MinLishOnSurface,
    onSurface = MinLishSurface,
    surfaceVariant = MinLishOnSurfaceVariant,
    onSurfaceVariant = MinLishSurfaceContainerHighest,
    error = MinLishErrorContainer,
    onError = MinLishOnErrorContainer,
    errorContainer = MinLishError,
    onErrorContainer = MinLishOnError,
    outline = MinLishOutlineVariant,
    outlineVariant = MinLishOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MinLishPrimary,
    onPrimary = MinLishOnPrimary,
    primaryContainer = MinLishPrimaryContainer,
    onPrimaryContainer = MinLishOnPrimaryContainer,
    secondary = MinLishSecondary,
    onSecondary = MinLishOnSecondary,
    secondaryContainer = MinLishSecondaryContainer,
    onSecondaryContainer = MinLishOnSecondaryContainer,
    tertiary = MinLishTertiary,
    onTertiary = MinLishOnTertiary,
    tertiaryContainer = MinLishTertiaryContainer,
    background = MinLishBackground,
    onBackground = MinLishOnSurface,
    surface = MinLishSurface,
    onSurface = MinLishOnSurface,
    surfaceVariant = MinLishSurfaceVariant,
    onSurfaceVariant = MinLishOnSurfaceVariant,
    surfaceDim = MinLishSurfaceDim,
    surfaceBright = MinLishSurfaceBright,
    surfaceContainerLowest = MinLishSurfaceContainerLowest,
    surfaceContainerLow = MinLishSurfaceContainerLow,
    surfaceContainer = MinLishSurfaceContainer,
    surfaceContainerHigh = MinLishSurfaceContainerHigh,
    surfaceContainerHighest = MinLishSurfaceContainerHighest,
    outline = MinLishOutline,
    outlineVariant = MinLishOutlineVariant,
    error = MinLishError,
    onError = MinLishOnError,
    errorContainer = MinLishErrorContainer,
    onErrorContainer = MinLishOnErrorContainer,
    primaryFixed = MinLishPrimaryFixed,
    primaryFixedDim = MinLishPrimaryFixedDim,
    tertiaryFixed = MinLishTertiaryFixed,
    tertiaryFixedDim = MinLishTertiaryFixedDim
)

@Composable
fun MinLishAppLearnEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MinLishShapes,
        content = content
    )
}
