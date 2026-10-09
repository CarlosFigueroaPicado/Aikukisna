package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.LeccionViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.GreenSecondary
import com.aikukisna.app.ui.theme.RedSecondary
import com.aikukisna.app.domain.usecase.ObtenerMapaLeccionesUseCase
import androidx.compose.runtime.getValue

@Composable
fun LeccionQuizScreen(
    viewModel: LeccionViewModel = hiltViewModel(),
    leccionId: Int,
    onCompletado: (correctas: Int, total: Int, palabras: Int, aprobado: Boolean) -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(leccionId) {
        viewModel.cargar(leccionId)
        viewModel.cargarQuiz()
    }

    LeccionQuizScreenContenido(
        isLoading = viewModel.isLoadingQuiz,
        errorMessage = viewModel.errorMessage,
        preguntas = viewModel.preguntas,
        tituloLeccion = viewModel.tituloLeccion,
        indicePregunta = viewModel.indicePregunta,
        opcionSeleccionada = viewModel.opcionSeleccionada,
        onSeleccionarOpcion = viewModel::seleccionarOpcion,
        onSiguienteClick = {
            val total = viewModel.preguntas.size
            if (viewModel.siguientePregunta()) {
                val porcentaje = if (total == 0) 0 else (viewModel.respuestasCorrectas * 100) / total
                val aprobado = porcentaje >= ObtenerMapaLeccionesUseCase.PORCENTAJE_APROBACION
                if (aprobado) {
                    viewModel.completarLeccion(porcentaje) {
                        onCompletado(viewModel.respuestasCorrectas, total, viewModel.vocabulario.size, true)
                    }
                } else {
                    viewModel.registrarIntento(porcentaje) {
                        onCompletado(viewModel.respuestasCorrectas, total, viewModel.vocabulario.size, false)
                    }
                }
            }
        },
        onVolver = onVolver
    )
}

@Composable
private fun LeccionQuizScreenContenido(
    isLoading: Boolean,
    errorMessage: String?,
    preguntas: List<PreguntaQuiz>,
    tituloLeccion: String,
    indicePregunta: Int,
    opcionSeleccionada: String?,
    onSeleccionarOpcion: (String) -> Unit,
    onSiguienteClick: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = t(R.string.leccionquiz_volver),
                modifier = Modifier.size(20.dp).clickable(onClick = onVolver)
            )
            Text(tituloLeccion.uppercase().ifBlank { t(R.string.leccionquiz_leccion) }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
            preguntas.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(t(R.string.leccionquiz_no_se_pudo_generar_el), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                val total = preguntas.size
                val pregunta = preguntas[indicePregunta]
                val fraccionProgreso by animateFloatAsState(
                    targetValue = (indicePregunta + 1f) / total,
                    animationSpec = tween(durationMillis = 350),
                    label = "fraccionProgresoQuizLeccion"
                )

                Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp, vertical = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t(R.string.leccionquiz_quiz), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(t(R.string.leccionquiz_texto, indicePregunta + 1, total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(11.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraccionProgreso)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(t(R.string.leccionquiz_selecciona_la_opcion_correcta), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        // El texto con estilos no llegaba a los lectores de pantalla: se declara completo.
                        modifier = Modifier.semantics { contentDescription = pregunta.textoPregunta; heading() },
                        text = preguntaAnotadaLeccion(pregunta, MaterialTheme.colorScheme.primary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pregunta.opciones.forEach { opcion ->
                            OpcionQuizLeccion(
                                texto = opcion,
                                estado = when {
                                    opcionSeleccionada == null -> EstadoOpcionLeccion.Neutral
                                    opcion == pregunta.respuestaCorrecta -> EstadoOpcionLeccion.Correcta
                                    opcion == opcionSeleccionada -> EstadoOpcionLeccion.Incorrecta
                                    else -> EstadoOpcionLeccion.Neutral
                                },
                                habilitada = opcionSeleccionada == null,
                                onClick = { onSeleccionarOpcion(opcion) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(bottom = 32.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(modifier = Modifier.width(200.dp)) {
                        AikukisnaButton(
                            text = t(R.string.leccionquiz_siguiente),
                            onClick = onSiguienteClick,
                            enabled = opcionSeleccionada != null,
                            trailingIcon = R.drawable.arrow_right
                        )
                    }
                }
            }
        }
    }
}

private fun preguntaAnotadaLeccion(pregunta: PreguntaQuiz, colorResaltado: Color) = buildAnnotatedString {
    val partes = pregunta.textoPregunta.split("\"")
    partes.forEachIndexed { indice, parte ->
        if (indice == 1) {
            withStyle(SpanStyle(color = colorResaltado)) { append("\"$parte\"") }
        } else {
            append(parte)
        }
    }
}

private enum class EstadoOpcionLeccion { Neutral, Correcta, Incorrecta }

@Composable
private fun OpcionQuizLeccion(
    texto: String,
    estado: EstadoOpcionLeccion,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val colorBorde = when (estado) {
        EstadoOpcionLeccion.Correcta -> GreenSecondary
        EstadoOpcionLeccion.Incorrecta -> RedSecondary
        EstadoOpcionLeccion.Neutral -> MaterialTheme.colorScheme.outlineVariant
    }
    val colorTexto = when (estado) {
        EstadoOpcionLeccion.Correcta -> GreenSecondary
        EstadoOpcionLeccion.Incorrecta -> RedSecondary
        EstadoOpcionLeccion.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val colorFondo = when (estado) {
        EstadoOpcionLeccion.Correcta -> GreenSecondary.copy(alpha = 0.1f)
        EstadoOpcionLeccion.Incorrecta -> RedSecondary.copy(alpha = 0.1f)
        EstadoOpcionLeccion.Neutral -> MaterialTheme.colorScheme.background
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorFondo, RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = colorBorde, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = habilitada, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyMedium, color = colorTexto, modifier = Modifier.weight(1f))
        if (estado == EstadoOpcionLeccion.Incorrecta) {
            Icon(
                painter = painterResource(id = R.drawable.x),
                contentDescription = null,
                tint = RedSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private val preguntaDeMuestra = PreguntaQuiz(
    textoPregunta = "¿Qué significa \"Tingki\"?",
    respuestaCorrecta = "Gracias",
    opciones = listOf("Gracias", "Adiós", "Buenos días")
)

@Preview(showBackground = true, name = "Sin responder")
@Composable
private fun LeccionQuizScreenContenidoPreview() {
    AikukisnaTheme {
        LeccionQuizScreenContenido(
            isLoading = false, errorMessage = null, preguntas = listOf(preguntaDeMuestra), tituloLeccion = "Saludos y despedidas",
            indicePregunta = 0, opcionSeleccionada = null,
            onSeleccionarOpcion = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Respondida")
@Composable
private fun LeccionQuizScreenContenidoRespondidaPreview() {
    AikukisnaTheme {
        LeccionQuizScreenContenido(
            isLoading = false, errorMessage = null, preguntas = listOf(preguntaDeMuestra), tituloLeccion = "Saludos y despedidas",
            indicePregunta = 0, opcionSeleccionada = "Gracias",
            onSeleccionarOpcion = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun LeccionQuizScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        LeccionQuizScreenContenido(
            isLoading = true, errorMessage = null, preguntas = emptyList(), tituloLeccion = "Saludos y despedidas",
            indicePregunta = 0, opcionSeleccionada = null,
            onSeleccionarOpcion = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}
