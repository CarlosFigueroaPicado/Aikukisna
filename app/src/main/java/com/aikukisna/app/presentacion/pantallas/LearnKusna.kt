package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.presentacion.componentes.BarraProgreso
import com.aikukisna.app.presentacion.componentes.NivelInfo
import com.aikukisna.app.presentacion.componentes.SelectorNiveles
import com.aikukisna.app.presentacion.componentes.SeparadorNivel
import com.aikukisna.app.presentacion.viewmodel.LearnUiState
import com.aikukisna.app.presentacion.viewmodel.LearnViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import kotlinx.coroutines.launch

@Composable
fun LearnScreen(
    viewModel: LearnViewModel = hiltViewModel(),
    onLeccionClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.padding(16.dp))
    {
        when (val state = uiState) {
            LearnUiState.Cargando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is LearnUiState.Exito -> {
                LearnContent(
                    lecciones = state.lecciones,
                    onLeccionClick = onLeccionClick
                )
            }
            is LearnUiState.Error -> {
                Text(
                    text = state.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LearnContent(
    lecciones: List<Leccion>,
    onLeccionClick: (String) -> Unit = {}
) {
    val listaNiveles = remember {
        listOf(
            NivelInfo("A0", "Superviviente", 0, 9),
            NivelInfo("A1", "Principiante", 0, 12),
            NivelInfo("A2", "Elemental", 0, 15),
            NivelInfo("B1", "Intermedio", 0, 20),
            NivelInfo("B2", "Intermedio Alto", 0, 18),
            NivelInfo("C1", "Avanzado", 0, 10),
            NivelInfo("C2", "Maestría", 0, 8)
        )
    }

    val codigosANivel = remember {
        mapOf("A0" to 0, "A1" to 1, "A2" to 2, "B1" to 3, "B2" to 4, "C1" to 5, "C2" to 6)
    }

    var nivelCodigoSeleccionado by remember { mutableIntStateOf(0) }
    val codigoActual = codigosANivel.entries.firstOrNull { it.value == nivelCodigoSeleccionado }?.key ?: "A0"

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Agrupar lecciones por nivel
    val leccionesPorNivel = remember(lecciones) {
        lecciones.groupBy { it.nivel }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "header_main") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lecciones",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.racha),
                            contentDescription = "Racha",
                            modifier = Modifier.size(35.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Miskito",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2/37",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BarraProgreso(
                    progress = 0.2f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectorNiveles(
                    niveles = listaNiveles,
                    nivelSeleccionado = codigoActual,
                    onNivelSelected = { nivel ->
                        val nuevoIndex = codigosANivel[nivel.codigo] ?: 0
                        nivelCodigoSeleccionado = nuevoIndex
                        coroutineScope.launch {
                            val targetKey = "header_${nivel.codigo}"
                            val itemIndex = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { it.key == targetKey }?.index

                            if (itemIndex != null) {
                                listState.animateScrollToItem(itemIndex)
                            }
                        }
                    }
                )
            }
        }

        listaNiveles.forEach { nivelInfo ->
            val numNivel = codigosANivel[nivelInfo.codigo] ?: 0
            val leccionesDelNivel = leccionesPorNivel[numNivel] ?: emptyList()

            item(key = "header_${nivelInfo.codigo}") {
                SeparadorNivel(
                    info = nivelInfo,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (leccionesDelNivel.isEmpty()) {
                item(key = "empty_${nivelInfo.codigo}") {
                    Text(
                        text = "Próximamente...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                itemsIndexed(
                    items = leccionesDelNivel,
                    key = { _, leccion -> leccion.id }
                ) { index, leccion ->

                    val offsetX = when (index % 4) {
                        1 -> 32.dp
                        3 -> (-32).dp
                        else -> 0.dp
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PuntosConectores()

                        NodoLeccion(
                            leccion = leccion,
                            modifier = Modifier.offset(x = offsetX),
                            onClick = { onLeccionClick(leccion.id.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NodoLeccion(
    leccion: Leccion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .drawBehind {
                drawCircle(
                    color = Color(0xFF6C6C6C),
                    radius = size.minDimension / 2,
                    center = center.copy(y = center.y + 6.dp.toPx()),
                    alpha = 0.2f,

                )
            }
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.libro),
            contentDescription = leccion.titulo,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun PuntosConectores() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6C6C6C))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LearnScreenPreview() {
    AikukisnaTheme {
        LearnContent(
            lecciones = listOf(
                Leccion(id = 1, titulo = "Lección 1", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 2, titulo = "Lección 2", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 3, titulo = "Lección 3", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 4, titulo = "Lección 4", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 5, titulo = "Lección 5", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 6, titulo = "Lección 6", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 7, titulo = "Lección 7", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 8, titulo = "Lección 8", capituloNumero = 1, nivel = 2, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                Leccion(id = 9, titulo = "Lección 9", capituloNumero = 1, nivel = 2, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                ),
            onLeccionClick = {}
        )
    }
}