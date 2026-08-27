package com.aikukisna.app.ui.theme

import androidx.compose.ui.graphics.Color


val OrangePrimary = Color(0xFFFD8B23)
val OrangePressed = Color(0xFFE87512)
val BluePrimary = Color(0xFF0A518C)

val YellowPrimary = Color(0xFFF4C542)

// --- Colores Secundarios / Funcionales (verificados) ---
val GreenSecondary = Color(0xFF51C87C)     // brand/success — antes 0xFF539643
val CyanSecondary = Color(0xFF53BAC6)      // brand/info — antes 0xFF3B9AC4
val RedSecondary = Color(0xFFD6434D)       // brand/error — antes 0xFFBA513A

// --- Colores Neutros (verificados) ---
val DarkNeutral = Color(0xFF242421)        // content/primary — antes 0xFF050505 (era casi negro puro, el real es más cálido)
val LightNeutral = Color(0xFFFFFFFF)       // surface/background — antes 0xFFF5F5F5
val DarkGray = Color(0xFF2D2D2D)           // SIN VERIFICAR — ninguna pantalla capturada usa modo oscuro
val LightGray = Color(0xFFE2E2DE)          // border/default — antes 0xFFD4D1D1
val MediumGray = Color(0xFF686863)         // content/secondary — antes 0xFFA6A19A (era mucho más claro que el real)

// --- Nuevos, no existían pero se usan en todas las pantallas capturadas ---
val CardSurface = Color(0xFFFDFDFD)        // surface/cards — fondo de tarjetas e inputs, apenas distinto del blanco puro
val BorderStrong = Color(0xFFB8B8B2)       // border/strong — paginación inactiva, elementos deshabilitados
val BrandSubtle = Color(0xFFFFF3E8)        // surface/brandSubtle — fondo de tags, riel de barras de progreso
val ContentInverse = Color(0xFFF9F9F9)     // content/inverse — texto sobre superficies de color