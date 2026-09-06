package com.aikukisna.app.presentacion.pantallas

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.usecase.ItemVocabularioLeccion
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.LeccionViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.OrangePressed
import androidx.compose.runtime.getValue

@Composable
fun LeccionVocabularioScreen(
    viewModel: LeccionViewModel = hiltViewModel(),
    leccionId: Int,
    onCompletado: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(leccionId) { viewModel.cargar(leccionId) }

    LeccionVocabularioScreenContenido(
        isLoading = viewModel.isLoadingVocabulario,
        errorMessage = viewModel.errorMessage,
        vocabulario = viewModel.vocabulario,
        indiceActual = viewModel.indiceActual,
        tarjetaVolteada = viewModel.tarjetaVolteada,
        onVoltear = viewModel::voltearTarjeta,
        onSiguienteClick = { if (viewModel.siguienteTarjeta()) onCompletado() },
        onVolver = onVolver
    )
}

@Composable
private fun LeccionVocabularioScreenContenido(
    isLoading: Boolean,
    errorMessage: String?,
    vocabulario: List<ItemVocabularioLeccion>,
    indiceActual: Int,
    tarjetaVolteada: Boolean,
    onVoltear: () -> Unit,
    onSiguienteClick: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                contentDescription = "Volver",
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = "Vocabulario",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
            vocabulario.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Esta lección no tiene contenido todavía.", color = MediumGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                val total = vocabulario.size
                val actual = vocabulario[indiceActual]

                Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vocabulario", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text("${indiceActual + 1}/$total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(11.dp).clip(RoundedCornerShape(8.dp)).background(BrandSubtle)) {
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
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp),
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
                    TarjetaVocabularioLeccion(item = actual, volteada = tarjetaVolteada, onVoltear = onVoltear)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(bottom = 32.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(modifier = Modifier.width(200.dp)) {
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
    }
}

@Composable
private fun TarjetaVocabularioLeccion(
    item: ItemVocabularioLeccion,
    volteada: Boolean,
    onVoltear: () -> Unit
) {
    val rotacion by animateFloatAsState(
        targetValue = if (volteada) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flipTarjetaLeccion"
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 220.dp)
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
            Text(
                text = if (rotacion <= 90f) (item.textoDestino ?: item.textoOrigen) else item.textoOrigen,
                style = MaterialTheme.typography.headlineSmall,
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
            Column(
                modifier = Modifier.graphicsLayer { rotationY = 180f },
                horizontalAlignment = Alignment.CenterHorizontally
            ) { contenido() }
        }
    }
}

private val itemDeMuestra = ItemVocabularioLeccion(textoOrigen = "Tingki", textoDestino = "Gracias")

@Preview(showBackground = true, name = "Sin voltear")
@Composable
private fun LeccionVocabularioScreenContenidoPreview() {
    AikukisnaTheme {
        LeccionVocabularioScreenContenido(
            isLoading = false, errorMessage = null, vocabulario = listOf(itemDeMuestra),
            indiceActual = 0, tarjetaVolteada = false,
            onVoltear = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Volteada")
@Composable
private fun LeccionVocabularioScreenContenidoVolteadaPreview() {
    AikukisnaTheme {
        LeccionVocabularioScreenContenido(
            isLoading = false, errorMessage = null, vocabulario = listOf(itemDeMuestra),
            indiceActual = 0, tarjetaVolteada = true,
            onVoltear = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun LeccionVocabularioScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        LeccionVocabularioScreenContenido(
            isLoading = true, errorMessage = null, vocabulario = emptyList(),
            indiceActual = 0, tarjetaVolteada = false,
            onVoltear = {}, onSiguienteClick = {}, onVolver = {}
        )
    }
}