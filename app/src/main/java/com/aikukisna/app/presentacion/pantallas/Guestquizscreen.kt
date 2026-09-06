package com.aikukisna.app.presentacion.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.acentoPara
import com.aikukisna.app.presentacion.viewmodel.GuestLeccionViewModel
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.GreenSecondary
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.RedSecondary

@Composable
fun GuestQuizScreen(
    viewModel: GuestLeccionViewModel = hiltViewModel(),
    idioma: Idioma,
    onCompletado: (correctas: Int, total: Int) -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(idioma.id) { viewModel.cargarQuiz(idioma.id) }

    val acento = acentoPara(idioma)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        Column {
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
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onVolver)
                )
                Text(
                    text = idioma.nombre.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = acento.color
                )
            }
            HorizontalDivider(color = LightGray)
        }

        BannerRegistrate()

        if (viewModel.isLoadingQuiz || viewModel.preguntas.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = acento.color)
            }
            return@Column
        }

        val total = viewModel.preguntas.size
        val pregunta = viewModel.preguntas[viewModel.indicePregunta]
        val seleccionada = viewModel.opcionSeleccionada
        val esCorrecta = seleccionada != null && seleccionada == pregunta.respuestaCorrecta


        val fraccionProgreso by animateFloatAsState(
            targetValue = (viewModel.indicePregunta + 1f) / total,
            animationSpec = tween(durationMillis = 350),
            label = "fraccionProgresoQuiz"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quiz",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${viewModel.indicePregunta + 1}/$total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccionProgreso)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TukiReactivo(disparador = seleccionada, esCorrecta = esCorrecta)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selecciona la opción correcta",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = preguntaAnotada(pregunta, MaterialTheme.colorScheme.primary),
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pregunta.opciones.forEach { opcion ->
                    OpcionQuiz(
                        texto = opcion,
                        estado = when {
                            seleccionada == null -> EstadoOpcion.Neutral
                            opcion == pregunta.respuestaCorrecta -> EstadoOpcion.Correcta
                            opcion == seleccionada -> EstadoOpcion.Incorrecta
                            else -> EstadoOpcion.Neutral
                        },
                        habilitada = seleccionada == null,
                        onClick = { viewModel.seleccionarOpcion(opcion) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SALTAR",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable {
                    if (viewModel.siguientePregunta()) {
                        onCompletado(viewModel.respuestasCorrectas, total)
                    }
                }
            )
            Box(modifier = Modifier.width(160.dp)) {
                AikukisnaButton(
                    text = "Siguiente",
                    onClick = {
                        if (viewModel.siguientePregunta()) {
                            onCompletado(viewModel.respuestasCorrectas, total)
                        }
                    },
                    enabled = seleccionada != null,
                    trailingIcon = R.drawable.arrow_right
                )
            }
        }
    }
}

private fun preguntaAnotada(pregunta: PreguntaQuiz, colorResaltado: Color) = buildAnnotatedString {

    val partes = pregunta.textoPregunta.split("\"")
    partes.forEachIndexed { indice, parte ->
        if (indice == 1) {
            withStyle(SpanStyle(color = colorResaltado)) {
                append("\"$parte\"")
            }
        } else {
            append(parte)
        }
    }
}

@Composable
private fun TukiReactivo(disparador: String?, esCorrecta: Boolean) {
    val escala = remember { Animatable(1f) }

    LaunchedEffect(disparador) {
        if (disparador != null && esCorrecta) {
            escala.animateTo(1.25f, animationSpec = tween(150))
            escala.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Image(
        painter = painterResource(id = R.drawable.ic_ave_login),
        contentDescription = null,
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
            }
    )
}

@Composable
private fun BannerRegistrate() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSubtle)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MODO INVITADO · Regístrate para guardar tu progreso",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

private enum class EstadoOpcion { Neutral, Correcta, Incorrecta }

@Composable
private fun OpcionQuiz(
    texto: String,
    estado: EstadoOpcion,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val colorBorde = when (estado) {
        EstadoOpcion.Correcta -> GreenSecondary
        EstadoOpcion.Incorrecta -> RedSecondary
        EstadoOpcion.Neutral -> LightGray
    }
    val colorTexto = when (estado) {
        EstadoOpcion.Correcta -> GreenSecondary
        EstadoOpcion.Incorrecta -> RedSecondary
        EstadoOpcion.Neutral -> MediumGray
    }
    val colorFondo = when (estado) {
        EstadoOpcion.Correcta -> GreenSecondary.copy(alpha = 0.1f)
        EstadoOpcion.Incorrecta -> RedSecondary.copy(alpha = 0.1f)
        EstadoOpcion.Neutral -> MaterialTheme.colorScheme.background
    }


    val density = LocalDensity.current
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(estado) {
        if (estado == EstadoOpcion.Incorrecta) {
            listOf(8f, -8f, 6f, -6f, 0f).forEach { valorDp ->
                offsetX.animateTo(with(density) { valorDp.dp.toPx() }, animationSpec = tween(45, easing = LinearEasing))
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = offsetX.value }
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = colorBorde, shape = RoundedCornerShape(8.dp))
            .background(color = colorFondo, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = habilitada, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = colorTexto,
            modifier = Modifier.weight(1f)
        )
        when (estado) {
            EstadoOpcion.Correcta -> Text(text = "\u2705", style = MaterialTheme.typography.bodySmall)
            EstadoOpcion.Incorrecta -> Icon(
                painter = painterResource(id = R.drawable.x),
                contentDescription = null,
                tint = RedSecondary,
                modifier = Modifier.size(14.dp)
            )
            EstadoOpcion.Neutral -> Unit
        }
    }
}