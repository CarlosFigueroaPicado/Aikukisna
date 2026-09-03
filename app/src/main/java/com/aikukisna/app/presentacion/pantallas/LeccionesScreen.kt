package com.aikukisna.app.presentacion.pantallas

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray

@Composable
fun LeccionesScreen(
    viewModel: LeccionesViewModel = hiltViewModel(),
    onAbrirLeccion: (Int) -> Unit = {}
) {
    LeccionesScreenContenido(
        nivelSeleccionado = viewModel.nivelSeleccionado,
        onNivelSeleccionado = viewModel::seleccionarNivel,
        lecciones = viewModel.lecciones,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onAbrirLeccion = onAbrirLeccion
    )
}

@Composable
private fun LeccionesScreenContenido(
    nivelSeleccionado: Int,
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
            text = "Lecciones",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
        )

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
                    onClick = { onNivelSeleccionado(nivel) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            lecciones.isEmpty() -> {
                Text(
                    text = "Todavía no hay lecciones en este nivel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray
                )
            }
            else -> {
                LazyColumn {
                    itemsIndexed(lecciones) { index, item ->
                        NodoLeccionRow(
                            item = item,
                            esPrimero = index == 0,
                            esUltimo = index == lecciones.lastIndex,
                            onClick = { onAbrirLeccion(item.leccion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipNivel(
    etiqueta: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (seleccionado) Color.Transparent else LightGray,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = if (seleccionado) Color.White else MediumGray
        )
    }
}

@Composable
private fun NodoLeccionRow(
    item: LeccionConEstado,
    esPrimero: Boolean,
    esUltimo: Boolean,
    onClick: () -> Unit
) {
    val habilitado = item.estado != EstadoLeccion.BLOQUEADA

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LineaConectora(visible = !esPrimero)
            NodoIcono(estado = item.estado)
            LineaConectora(visible = !esUltimo)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = item.leccion.titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (habilitado) MaterialTheme.colorScheme.onBackground else MediumGray
            )
            Text(
                text = when (item.estado) {
                    EstadoLeccion.COMPLETADA -> "Completada · ${item.puntaje ?: 0} pts"
                    EstadoLeccion.ACTUAL -> "Lección actual"
                    EstadoLeccion.BLOQUEADA -> "Bloqueada"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray
            )
        }
    }
}

@Composable
private fun LineaConectora(visible: Boolean) {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(14.dp)
            .background(if (visible) LightGray else Color.Transparent)
    )
}

@Composable
private fun NodoIcono(estado: EstadoLeccion) {
    val colorFondo = when (estado) {
        EstadoLeccion.COMPLETADA, EstadoLeccion.ACTUAL -> MaterialTheme.colorScheme.primary
        EstadoLeccion.BLOQUEADA -> LightGray
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        when (estado) {
            EstadoLeccion.COMPLETADA -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completada",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            EstadoLeccion.ACTUAL -> Icon(
                painter = painterResource(id = R.drawable.play),
                contentDescription = "Lección actual",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            EstadoLeccion.BLOQUEADA -> Icon(
                painter = painterResource(id = R.drawable.lock),
                contentDescription = "Bloqueada",
                tint = MediumGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private val idiomaDeMuestra = Idioma.DISPONIBLES.first { it.codigo == "mi" }

private val leccionesDeMuestra = listOf(
    LeccionConEstado(
        leccion = Leccion(id = 1, titulo = "Saludos y Despedidas", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = idiomaDeMuestra),
        estado = EstadoLeccion.COMPLETADA,
        puntaje = 90
    ),
    LeccionConEstado(
        leccion = Leccion(id = 2, titulo = "Familia", capituloNumero = 2, nivel = 1, categoria = null, idiomaMeta = idiomaDeMuestra),
        estado = EstadoLeccion.ACTUAL,
        puntaje = null
    ),
    LeccionConEstado(
        leccion = Leccion(id = 3, titulo = "Números", capituloNumero = 3, nivel = 1, categoria = null, idiomaMeta = idiomaDeMuestra),
        estado = EstadoLeccion.BLOQUEADA,
        puntaje = null
    )
)

@Preview(showBackground = true, name = "Con lecciones")
@Composable
private fun LeccionesScreenContenidoPreview() {
    AikukisnaTheme {
        LeccionesScreenContenido(
            nivelSeleccionado = 1,
            onNivelSeleccionado = {},
            lecciones = leccionesDeMuestra,
            isLoading = false,
            errorMessage = null,
            onAbrirLeccion = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun LeccionesScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        LeccionesScreenContenido(
            nivelSeleccionado = 1,
            onNivelSeleccionado = {},
            lecciones = emptyList(),
            isLoading = true,
            errorMessage = null,
            onAbrirLeccion = {}
        )
    }
}

@Preview(showBackground = true, name = "Sin lecciones en el nivel")
@Composable
private fun LeccionesScreenContenidoVacioPreview() {
    AikukisnaTheme {
        LeccionesScreenContenido(
            nivelSeleccionado = 7,
            onNivelSeleccionado = {},
            lecciones = emptyList(),
            isLoading = false,
            errorMessage = null,
            onAbrirLeccion = {}
        )
    }
}