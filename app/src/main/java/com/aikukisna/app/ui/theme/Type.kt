@file:OptIn(ExperimentalTextApi::class)

package com.aikukisna.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R

val Nunito = FontFamily(
    Font(
        resId = R.font.nunito_variablefont_wght,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.nunito_variablefont_wght,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.nunito_variablefont_wght,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.nunito_variablefont_wght,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.nunito_variablefont_wght,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

val AguDisplay = FontFamily(
    Font(
        R.font.agudisplay_regular_variable_font_,
        variationSettings = FontVariation.Settings(FontVariation.Setting("MORF", 30f))
    )
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrainsmono_variable_font_wght, FontWeight.Medium)
)

// Tamaños: los del documento de diseño + 1 sp (pedido del equipo, 8 de octubre de 2026; primero +2 y luego −1, para leer mejor en
// teléfonos pequeños). Interlineado 1.1x como en el documento. Tamaños originales en docs/auditoria_2026_10_08.md.
// Pesos e interlineado tomados directo del documento
// "Tipografías Aikukisna" que compartió el equipo de diseño — cada
// TextStyle de acá corresponde 1 a 1 con una fila de esa tabla.
val Typography = Typography(

    // Display — Agu Display Regular, 32pt
    displayLarge = TextStyle(
        fontFamily = AguDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 33.sp,
        lineHeight = 36.3.sp
    ),

    // Título 1 — Nunito negrita, 32pt
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 33.sp,
        lineHeight = 36.3.sp
    ),
    // Título 2 — Nunito seminegrita, 28pt
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 29.sp,
        lineHeight = 31.9.sp
    ),
    // Título 3 — Nunito seminegrita, 24pt
    titleSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 27.5.sp
    ),

    // Título 4 — Nunito medio, 20pt
    headlineSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 23.1.sp
    ),

    // Texto grande — Nunito Regular, 18pt
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 20.9.sp
    ),
    // Texto — Nunito Regular, 16pt
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 18.7.sp
    ),
    // Texto pequeño — Nunito Regular, 14pt
    bodySmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 16.5.sp
    ),
    // Etiqueta — Nunito medio, 14pt
    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 16.5.sp
    ),

    // Datos — JetBrains Mono regular, 13pt
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 15.4.sp
    ),

    // Estilos que antes tomaban el valor por defecto de Material 3, ahora +1 como el resto.
    labelSmall = androidx.compose.material3.Typography().labelSmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
    displaySmall = androidx.compose.material3.Typography().displaySmall.copy(fontSize = 37.sp, lineHeight = 45.sp)
)
