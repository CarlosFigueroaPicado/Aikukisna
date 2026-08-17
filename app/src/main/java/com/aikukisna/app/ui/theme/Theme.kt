package com.aikukisna.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange900,

    secondary = Blue500,
    onSecondary = White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue900,

    tertiary = Yellow500,
    onTertiary = Black,
    tertiaryContainer = Yellow100,
    onTertiaryContainer = Yellow900,

    background = AppBackground,
    onBackground = AppTextPrimary,

    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = White,
    onSurfaceVariant = AppTextSecondary,

    outline = AppBorder,
    outlineVariant = AppDisabled,

    error = Red500,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red900
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = Orange900,
    primaryContainer = Orange800,
    onPrimaryContainer = Orange100,

    secondary = Blue400,
    onSecondary = Blue900,
    secondaryContainer = Blue800,
    onSecondaryContainer = Blue100,

    tertiary = Yellow400,
    onTertiary = Yellow900,
    tertiaryContainer = Yellow800,
    onTertiaryContainer = Yellow100,

    background = Black,
    onBackground = White,

    surface = AppTextPrimary,
    onSurface = White,
    surfaceVariant = AppTextSecondary,
    onSurfaceVariant = AppSurface,

    outline = AppTextSecondary,
    outlineVariant = AppDisabled,

    error = Red400,
    onError = Red900,
    errorContainer = Red800,
    onErrorContainer = Red100
)

@Composable
fun AikukisnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )

}