package com.aikukisna.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- ESQUEMA MODO CLARO ---
private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = BluePrimary,
    onSecondary = Color.White,
    tertiary = YellowPrimary,
    onTertiary = DarkNeutral,
    background = LightNeutral,
    onBackground = DarkNeutral,
    surface = Color.White,
    onSurface = DarkNeutral,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    primaryContainer = BrandSubtle,
    onPrimaryContainer = OrangePressed,
    secondaryContainer = BlueSubtle,
    onSecondaryContainer = BlueDarkest,
    outline = BorderStrong,
    outlineVariant = LightGray,
    error = RedSecondary,
    onError = Color.White
)

// --- ESQUEMA MODO OSCURO ---
private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = CyanSecondary,        // Azul un poco más claro para destacar sobre fondo oscuro
    onSecondary = DarkNeutral,
    tertiary = YellowPrimary,
    onTertiary = DarkNeutral,
    background = DarkNeutral,
    onBackground = LightNeutral,
    surface = DarkGray,               // Las tarjetas contrastan sobre el fondo negro #050505
    onSurface = LightNeutral,
    surfaceVariant = Color(0xFF3A3A36),
    onSurfaceVariant = LightNeutral,
    primaryContainer = Color(0xFF4D2A0E),
    onPrimaryContainer = Color(0xFFFFDCC1),
    secondaryContainer = Color(0xFF173F45),
    onSecondaryContainer = Color(0xFFD5F4F7),
    outline = Color(0xFF9A9A93),
    outlineVariant = Color(0xFF51514C),
    error = RedSecondary,
    onError = Color.White
)

@Composable
fun AikukisnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Tu configuración en Type.kt
        content = content
    )
}
