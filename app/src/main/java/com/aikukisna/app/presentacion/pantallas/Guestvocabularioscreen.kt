package com.aikukisna.app.presentacion.pantallas

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.usecase.PalabraDemo
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.GuestLeccionViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BorderStrong
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.OrangePressed

@Composable
fun GuestVocabularioScreen(
    viewModel: GuestLeccionViewModel = hiltViewModel(),
    idioma: Idioma,
    onCompletado: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(idioma.id) { viewModel.cargar(idioma.id) }

    GuestVocabularioScreenContenido(
        idiomaNombre = idioma.nombre,
        isLoading = viewModel.isLoading,
        vocabulario = viewModel.vocabulario,
        indiceActual = viewModel.indiceActual,
        tarjetaVolteada = viewModel.tarjetaVolteada,
        autoevaluacion = viewModel.autoevaluacion,
        onVoltear = viewModel::voltearTarjeta,
        onAutoevaluar = viewModel::autoevaluar,
        onSiguienteClick = { if (viewModel.siguiente()) onCompletado() },
        onVolver = onVolver
    )
}

@Composable
private fun GuestVocabularioScreenContenido(
    idiomaNombre: String,
    isLoading: Boolean,
    vocabulario: List<PalabraDemo>,
    indiceActual: Int,
    tarjetaVolteada: Boolean,
    autoevaluacion: Boolean?,
    onVoltear: () -> Unit,
    onAutoevaluar: (Boolean) -> Unit,
    onSiguienteClick: () -> Unit,
    onVolver: () -> Unit
) {
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
                    text = idiomaNombre.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = LightGray)
        }

        BannerRegistrate()

        if (isLoading || vocabulario.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }

        val total = vocabulario.size
        val actual = vocabulario[indiceActual]

        Column(
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vocabulario",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${indiceActual + 1}/$total",
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
                        .fillMaxWidth((indiceActual + 1f) / total)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo se dice?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                TarjetaVocabulario(
                    item = actual,
                    volteada = tarjetaVolteada,
                    onVoltear = onVoltear
                )
                Spacer(modifier = Modifier.width(8.dp))
                TukiConPregunta(reaccionar = autoevaluacion == true)
            }

            if (tarjetaVolteada) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonAutoevaluacion(
                        texto = "Sí",
                        emoji = "\u2705",
                        relleno = autoevaluacion == true,
                        onClick = { onAutoevaluar(true) }
                    )
                    BotonAutoevaluacion(
                        texto = "No",
                        emoji = "\u274C",
                        relleno = autoevaluacion == false,
                        onClick = { onAutoevaluar(false) }
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
                modifier = Modifier.clickable(onClick = onSiguienteClick)
            )
            Box(modifier = Modifier.width(160.dp)) {
                AikukisnaButton(
                    text = "Siguiente",
                    onClick = onSiguienteClick,
                    enabled = tarjetaVolteada,
                    trailingIcon = R.drawable.arrow_right
                )
            }
        }
    }
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

private fun iconoParaConcepto(traduccionEspanol: String?): Int {
    val texto = traduccionEspanol?.trim()?.trimEnd('.', '¡', '!', '¿', '?')?.lowercase() ?: ""
    return when {
        texto.contains("buenos días") -> R.drawable.ic_concept_morning
        texto.contains("buenas tardes") -> R.drawable.ic_concept_afternoon
        texto.contains("buenas noches") -> R.drawable.ic_concept_night
        texto.contains("gracias") -> R.drawable.ic_concept_thanks
        texto.contains("por favor") -> R.drawable.ic_concept_please
        texto.contains("agua") -> R.drawable.ic_concept_water
        texto.contains("cómo está") || texto.contains("como esta") -> R.drawable.ic_concept_howareyou
        texto.contains("adiós") || texto.contains("adios") -> R.drawable.ic_concept_goodbye
        texto == "bien" -> R.drawable.ic_concept_good
        else -> R.drawable.ic_concept_howareyou // respaldo genérico si aparece un concepto nuevo
    }
}

@Composable
private fun TarjetaVocabulario(
    item: PalabraDemo,
    volteada: Boolean,
    onVoltear: () -> Unit
) {

    val rotacion by animateFloatAsState(
        targetValue = if (volteada) 180f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "flipTarjetaVocabulario"
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 220.dp)
            .graphicsLayer {
                rotationY = rotacion
                cameraDistance = 12f * density.density
            }
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(16.dp))
            .background(color = CardSurface, shape = RoundedCornerShape(16.dp))
            .then(if (!volteada) Modifier.clickable(onClick = onVoltear) else Modifier)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        val contenido: @Composable () -> Unit = {
            Icon(
                painter = painterResource(id = iconoParaConcepto(item.traduccionEspanol)),
                contentDescription = null,
                tint = OrangePressed,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (rotacion <= 90f) {
                    item.traduccionEspanol ?: item.palabra.texto
                } else {
                    item.palabra.texto
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (rotacion <= 90f) "Revelar traducción" else "Toca para voltear",
                style = MaterialTheme.typography.labelLarge,
                color = OrangePressed,
                textAlign = TextAlign.Center
            )
        }

        if (rotacion <= 90f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { contenido() }
        } else {
            // Contramarcada con su propio rotationY=180 para que el texto
            // no quede espejado (la tarjeta entera ya giró 180 por fuera).
            Column(
                modifier = Modifier.graphicsLayer { rotationY = 180f },
                horizontalAlignment = Alignment.CenterHorizontally
            ) { contenido() }
        }
    }
}

@Composable
private fun TukiConPregunta(reaccionar: Boolean) {
    val escala = remember { Animatable(1f) }

    LaunchedEffect(reaccionar) {
        if (reaccionar) {
            escala.animateTo(1.3f, animationSpec = tween(150))
            escala.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "\u2753", fontSize = 16.sp)
        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = escala.value
                    scaleY = escala.value
                }
        )
    }
}

@Composable
private fun BotonAutoevaluacion(
    texto: String,
    emoji: String,
    relleno: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (relleno) MaterialTheme.colorScheme.primary else BorderStrong.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = if (relleno) Color.White else MediumGray
        )
        Text(text = emoji, fontSize = 11.sp)
    }
}

private val palabraDemoDeMuestra = PalabraDemo(
    palabra = Palabra(
        id = 1,
        idioma = Idioma.DISPONIBLES.first { it.codigo == "mi" },
        texto = "Lî",
        categoria = null,
        fuente = FuenteDocumento(id = 1, titulo = "Muestra", autor = null, anio = null, institucion = null)
    ),
    traduccionEspanol = "Agua"
)

@Preview(showBackground = true, name = "Sin voltear")
@Composable
private fun GuestVocabularioScreenContenidoPreview() {
    AikukisnaTheme {
        GuestVocabularioScreenContenido(
            idiomaNombre = "Miskito",
            isLoading = false,
            vocabulario = listOf(palabraDemoDeMuestra),
            indiceActual = 0,
            tarjetaVolteada = false,
            autoevaluacion = null,
            onVoltear = {},
            onAutoevaluar = {},
            onSiguienteClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Volteada")
@Composable
private fun GuestVocabularioScreenContenidoVolteadaPreview() {
    AikukisnaTheme {
        GuestVocabularioScreenContenido(
            idiomaNombre = "Miskito",
            isLoading = false,
            vocabulario = listOf(palabraDemoDeMuestra),
            indiceActual = 0,
            tarjetaVolteada = true,
            autoevaluacion = true,
            onVoltear = {},
            onAutoevaluar = {},
            onSiguienteClick = {},
            onVolver = {}
        )
    }
}