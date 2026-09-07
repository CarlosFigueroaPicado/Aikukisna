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

// Tamaños, pesos e interlineado (1.1x) tomados directo del documento
// "Tipografías Aikukisna" que compartió el equipo de diseño — cada
// TextStyle de acá corresponde 1 a 1 con una fila de esa tabla.
val Typography = Typography(

    // Display — Agu Display Regular, 32pt
    displayLarge = TextStyle(
        fontFamily = AguDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 35.2.sp
    ),

    // Título 1 — Nunito negrita, 32pt
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 35.2.sp
    ),
    // Título 2 — Nunito seminegrita, 28pt
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 30.8.sp
    ),
    // Título 3 — Nunito seminegrita, 24pt
    titleSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 26.4.sp
    ),

    // Título 4 — Nunito medio, 20pt
    headlineSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 22.sp
    ),

    // Texto grande — Nunito Regular, 18pt
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 19.8.sp
    ),
    // Texto — Nunito Regular, 16pt
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 17.6.sp
    ),
    // Texto pequeño — Nunito Regular, 14pt
    bodySmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 15.4.sp
    ),
    // Etiqueta — Nunito medio, 14pt
    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 15.4.sp
    ),

    // Datos — JetBrains Mono regular, 13pt
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 14.3.sp
    )
)
