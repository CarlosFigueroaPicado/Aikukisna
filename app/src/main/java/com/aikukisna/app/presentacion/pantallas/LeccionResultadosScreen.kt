package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.FrasesIdioma
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.domain.usecase.ObtenerMapaLeccionesUseCase
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun LeccionResultadosScreen(
    respuestasCorrectas: Int,
    totalPreguntas: Int,
    palabrasAprendidas: Int,
    aprobado: Boolean,
    xpGanado: Int = 0,
    onVolver: () -> Unit,
    idiomaId: Int? = null
) {

    val porcentaje = if (totalPreguntas == 0) 0 else (respuestasCorrectas * 100) / totalPreguntas
    // XP real que sumó el intento (20 + bono la primera vez; al repetir solo la mejora del bono).
    val xpEstimado = if (aprobado) xpGanado else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (aprobado) {
                    t(R.string.leccionresultados_excelente_superada) + "\n" + FrasesIdioma.felicitacion(idiomaId)
                } else {
                    t(R.string.leccionresultados_necesitas_para_aprobar, ObtenerMapaLeccionesUseCase.PORCENTAJE_APROBACION)
                },
                style = if (aprobado) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Image(
            painter = painterResource(id = if (aprobado) R.drawable.tuki_celebrating else R.drawable.tuki_teaching),
            contentDescription = null,
            modifier = Modifier.size(width = 134.dp, height = 120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (aprobado) t(R.string.leccionresultados_lo_lograste) else t(R.string.leccionresultados_intentalo_nuevamente),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (aprobado) {
                t(R.string.leccionresultados_de_respuestas_correctas, respuestasCorrectas, totalPreguntas)
            } else {
                t(R.string.leccionresultados_la_siguiente_leccion_continuara_bloqueada)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaResultado(valor = t(R.string.leccionresultados_texto, palabrasAprendidas), etiqueta = t(R.string.leccionresultados_palabras_aprendidas))
            EstadisticaResultado(valor = t(R.string.leccionresultados_texto_2, respuestasCorrectas, totalPreguntas), etiqueta = t(R.string.leccionresultados_correctas))
            EstadisticaResultado(valor = t(R.string.leccionresultados_texto_3, xpEstimado), etiqueta = t(R.string.leccionresultados_xp_ganados))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.width(240.dp)) {
            AikukisnaButton(
                text = if (aprobado) t(R.string.leccionresultados_continuar) else t(R.string.leccionresultados_volver_a_las_lecciones),
                onClick = onVolver,
                trailingIcon = if (aprobado) R.drawable.arrow_right else R.drawable.ic_arrow_back
            )
        }
    }
}

@Composable
private fun EstadisticaResultado(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, name = "Resultado bueno")
@Composable
private fun LeccionResultadosScreenPreview() {
    AikukisnaTheme { LeccionResultadosScreen(respuestasCorrectas = 8, totalPreguntas = 8, palabrasAprendidas = 5, aprobado = true, onVolver = {}) }
}

@Preview(showBackground = true, name = "Resultado parcial")
@Composable
private fun LeccionResultadosScreenParcialPreview() {
    AikukisnaTheme { LeccionResultadosScreen(respuestasCorrectas = 3, totalPreguntas = 8, palabrasAprendidas = 5, aprobado = false, onVolver = {}) }
}
