package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.usecase.EstadoLeccion
import com.aikukisna.app.domain.usecase.LeccionConEstado
import com.aikukisna.app.presentacion.viewmodel.LeccionesViewModel
import com.aikukisna.app.presentacion.viewmodel.NIVELES_CEFR
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun LeccionesScreen(
    viewModel: LeccionesViewModel = hiltViewModel(),
    onAbrirLeccion: (Int) -> Unit = {}
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    LeccionesScreenContenido(
        idioma = viewModel.idiomaMetaNombre,
        nivelSeleccionado = viewModel.nivelSeleccionado,
        nivelesDesbloqueados = viewModel.nivelesDesbloqueados,
        onNivelSeleccionado = viewModel::seleccionarNivel,
        lecciones = viewModel.lecciones,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onAbrirLeccion = onAbrirLeccion
    )
}

@Composable
private fun LeccionesScreenContenido(
    idioma: String,
    nivelSeleccionado: Int,
    nivelesDesbloqueados: Set<Int>,
    onNivelSeleccionado: (Int) -> Unit,
    lecciones: List<LeccionConEstado>,
    isLoading: Boolean,
    errorMessage: String?,
    onAbrirLeccion: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = t(R.string.lecciones_lecciones),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (idioma.isNotBlank()) {
            Text(
                text = idioma,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NIVELES_CEFR.forEach { (nivel, etiqueta) ->
                ChipNivel(
                    etiqueta = etiqueta,
                    seleccionado = nivel == nivelSeleccionado,
                    habilitado = nivel in nivelesDesbloqueados
                ) { onNivelSeleccionado(nivel) }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            errorMessage != null -> Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            lecciones.isEmpty() -> Text(
                text = t(R.string.lecciones_todavia_no_hay_lecciones_en),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> {
                val etiquetaNivel = NIVELES_CEFR.first { it.first == nivelSeleccionado }.second
                val completadas = lecciones.count { it.estado == EstadoLeccion.COMPLETADA }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { ResumenNivel(etiquetaNivel, completadas, lecciones.size) }
                    itemsIndexed(lecciones) { index, item ->
                        NodoMapaLeccion(item, index) { onAbrirLeccion(item.leccion.id) }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ResumenNivel(etiqueta: String, completadas: Int, total: Int) {
    val descriptor = when (etiqueta) {
        "A0" -> t(R.string.lecciones_superviviente)
        "A1" -> t(R.string.lecciones_principiante)
        else -> null
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (descriptor == null) etiqueta else t(R.string.lecciones_texto, etiqueta, descriptor),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$completadas/$total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ChipNivel(etiqueta: String, seleccionado: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                1.dp,
                if (seleccionado) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                CircleShape
            )
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = when {
                seleccionado -> MaterialTheme.colorScheme.onPrimary
                habilitado -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
        )
    }
}

private val posicionesNodo = listOf(0.50f, 0.67f, 0.33f)

@Composable
private fun NodoMapaLeccion(item: LeccionConEstado, index: Int, onClick: () -> Unit) {
    val posicion = posicionesNodo[index % posicionesNodo.size]
    val posicionAnterior = posicionesNodo[(index - 1).mod(posicionesNodo.size)]
    val habilitada = item.estado != EstadoLeccion.BLOQUEADA
    val alineacionNodo = when (index % posicionesNodo.size) {
        1 -> Alignment.CenterEnd
        2 -> Alignment.CenterStart
        else -> Alignment.Center
    }
    val transicion = rememberInfiniteTransition(label = "leccionActual")
    val escalaActual by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsoLeccionActual"
    )

    Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
        if (index > 0) {
            val colorConector = MaterialTheme.colorScheme.outlineVariant
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = colorConector,
                    start = androidx.compose.ui.geometry.Offset(size.width * posicionAnterior, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width * posicion, size.height / 2f),
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 11f))
                )
            }
        }

        val escena = when (index % 3) {
            0 -> R.drawable.scene_mountain
            1 -> R.drawable.scenes_bush
            else -> R.drawable.scenes_lake
        }
        Image(
            painter = painterResource(escena),
            contentDescription = null,
            modifier = Modifier
                .align(if (posicion > 0.5f) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 8.dp)
                .size(width = if (index % 3 == 2) 116.dp else 90.dp, height = 68.dp)
        )

        Box(
            modifier = Modifier
                .align(alineacionNodo)
                .padding(horizontal = 84.dp)
                .size(68.dp)
                .graphicsLayer {
                    if (item.estado == EstadoLeccion.ACTUAL) {
                        scaleX = escalaActual
                        scaleY = escalaActual
                    }
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    2.dp,
                    if (item.estado == EstadoLeccion.ACTUAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    CircleShape
                )
                .clickable(enabled = habilitada, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (item.estado == EstadoLeccion.BLOQUEADA) {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = t(R.string.lecciones_bloqueada, item.leccion.titulo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.leccion_book),
                    contentDescription = item.leccion.titulo,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

private val idiomaDeMuestra = Idioma.DISPONIBLES.first { it.codigo == "mi" }
private val leccionesDeMuestra = listOf(
    LeccionConEstado(Leccion(1, "Saludos y Despedidas", 1, 1, null, idiomaDeMuestra), EstadoLeccion.COMPLETADA, 90),
    LeccionConEstado(Leccion(2, "Familia", 2, 1, null, idiomaDeMuestra), EstadoLeccion.ACTUAL, null),
    LeccionConEstado(Leccion(3, "Números", 3, 1, null, idiomaDeMuestra), EstadoLeccion.BLOQUEADA, null)
)

@Preview(showBackground = true)
@Composable
private fun LeccionesScreenContenidoPreview() {
    AikukisnaTheme {
        LeccionesScreenContenido(
            idioma = "Miskito",
            nivelSeleccionado = 1,
            nivelesDesbloqueados = setOf(1),
            onNivelSeleccionado = {},
            lecciones = leccionesDeMuestra,
            isLoading = false,
            errorMessage = null,
            onAbrirLeccion = {}
        )
    }
}
