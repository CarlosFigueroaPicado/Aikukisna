package com.aikukisna.app.presentacion.componentes

import androidx.compose.ui.graphics.Color
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.ui.theme.BluePrimary
import com.aikukisna.app.ui.theme.GreenSecondary
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.OrangePrimary
import com.aikukisna.app.ui.theme.YellowPrimary


data class AcentoIdioma(val color: Color, val inicial: String)

fun acentoPara(idioma: Idioma): AcentoIdioma = when (idioma.codigo) {
    "mi" -> AcentoIdioma(OrangePrimary, "MI")
    "es" -> AcentoIdioma(BluePrimary, "ES")
    "jam" -> AcentoIdioma(YellowPrimary, "KR")
    "en" -> AcentoIdioma(GreenSecondary, "EN")
    else -> AcentoIdioma(MediumGray, idioma.nombre.take(2).uppercase())
}