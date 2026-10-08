package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.InputStyle

import com.aikukisna.app.presentacion.viewmodel.DictionaryViewModel
import com.aikukisna.app.presentacion.viewmodel.PalabraConTraduccion
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel,
    onAbrirDetalle: (Int) -> Unit
) {
    DictionaryScreenContenido(
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        idiomaAprendizaje = viewModel.idiomaAprendizaje?.nombre,
        idiomaContraparte = viewModel.idiomaContraparte.nombre,
        onBuscar = viewModel::buscarAhora,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        resultados = viewModel.resultados,
        favoritosIds = viewModel.favoritosIds,
        onAlternarFavorito = viewModel::alternarFavorito,
        onAbrirDetalle = onAbrirDetalle,
        cargandoMas = viewModel.cargandoMas,
        hayMasResultados = viewModel.hayMasResultados,
        onCargarMas = viewModel::cargarMas
    )
}

@Composable
private fun DictionaryScreenContenido(
    query: String,
    onQueryChange: (String) -> Unit,
    idiomaAprendizaje: String?,
    idiomaContraparte: String = "Español",
    onBuscar: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    resultados: List<PalabraConTraduccion>,
    favoritosIds: Set<Int>,
    onAlternarFavorito: (Int) -> Unit,
    onAbrirDetalle: (Int) -> Unit,
    cargandoMas: Boolean = false,
    hayMasResultados: Boolean = false,
    onCargarMas: () -> Unit = {}
) {
    var categoriaSeleccionada by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(idiomaAprendizaje, query) {
        categoriaSeleccionada = null
    }
    val categorias = resultados
        .mapNotNull { item ->
            item.categoriaId?.let { id -> id to (item.categoriaNombre ?: t(R.string.dictionary_sin_categoria)) }
        }
        .distinctBy { it.first }
    val resultadosVisibles = if (categoriaSeleccionada == null) {
        resultados
    } else {
        resultados.filter { it.categoriaId == categoriaSeleccionada }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = t(R.string.dictionary_diccionario),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(14.dp))

        AikukisnaTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = idiomaAprendizaje?.let { t(R.string.dictionary_buscar_en_o, idiomaContraparte, it) }
                ?: t(R.string.dictionary_cargando_idioma_de_aprendizaje),
            enabled = idiomaAprendizaje != null,
            style = InputStyle.Outlined,
            leadingIcon = R.drawable.search,
            leadingIconContentDescription = t(R.string.dictionary_buscar),
            onLeadingIconClick = onBuscar
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (idiomaAprendizaje != null) Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoriaDiccionarioChip(
                texto = t(R.string.dictionary_todas),
                seleccionada = categoriaSeleccionada == null,
                onClick = { categoriaSeleccionada = null }
            )
            categorias.forEach { (id, nombre) ->
                CategoriaDiccionarioChip(
                    texto = nombre,
                    seleccionada = categoriaSeleccionada == id,
                    onClick = { categoriaSeleccionada = id }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (idiomaAprendizaje != null && query.isNotBlank()) {
            Text(
                text = if (resultadosVisibles.size == 1) t(R.string.dictionary_1_palabra) else t(R.string.dictionary_palabras, resultadosVisibles.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            idiomaAprendizaje == null -> {
                Text(
                    text = t(R.string.dictionary_cargando_el_idioma_elegido_en),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            query.isBlank() && resultadosVisibles.isEmpty() -> {
                Text(
                    text = t(R.string.dictionary_escribe_una_palabra_en_o, idiomaAprendizaje, idiomaContraparte),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            resultadosVisibles.isEmpty() -> {
                Text(
                    text = t(R.string.dictionary_sin_resultados_para, query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    resultadosVisibles
                        .groupBy { it.categoriaNombre ?: t(R.string.dictionary_diccionario) }
                        .forEach { (categoria, palabras) ->
                            item {
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                                )
                            }
                            items(palabras) { item ->
                                PalabraItem(
                                    item = item,
                                    esFavorita = item.palabraId in favoritosIds,
                                    onFavorito = onAlternarFavorito,
                                    onAbrirDetalle = onAbrirDetalle
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    // Exploración paginada (buscador vacío): al llegar al final se pide la siguiente página.
                    if (hayMasResultados) {
                        item {
                            if (cargandoMas) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LaunchedEffect(resultados.size) { onCargarMas() }
                            }
                        }
                    }
                }
            }
        }
    }
        if (resultadosVisibles.isNotEmpty()) {
            Image(
                painter = painterResource(R.drawable.tuki_like),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp)
                    .size(76.dp)
            )
        }
    }
}

@Composable
private fun CategoriaDiccionarioChip(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (seleccionada) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = if (seleccionada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PalabraItem(
    item: PalabraConTraduccion,
    esFavorita: Boolean,
    onFavorito: (Int) -> Unit,
    onAbrirDetalle: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                item.palabraId?.let { id -> Modifier.clickable { onAbrirDetalle(id) } }
                    ?: Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.esSugerencia) t(R.string.dictionary_quisiste_decir, item.texto) else item.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.traduccion != null) {
                        Text(
                            text = t(R.string.dictionary_texto, item.traduccion),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item.palabraId?.let { id ->
                Icon(
                    imageVector = if (esFavorita) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorita) t(R.string.dictionary_quitar_de_favoritos) else t(R.string.dictionary_agregar_a_favoritos),
                    tint = if (esFavorita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onFavorito(id) }
                )
            }
        }
    }
}

private val resultadosDeMuestra = listOf(
    PalabraConTraduccion(texto = "Tingki", traduccion = "Gracias"),
    PalabraConTraduccion(texto = "Aisabe", traduccion = "Adiós"),
    PalabraConTraduccion(texto = "Titan yamni", traduccion = "Buenos días")
)

@Preview(showBackground = true, name = "Con resultados")
@Composable
private fun DictionaryScreenContenidoPreview() {
    AikukisnaTheme {
        DictionaryScreenContenido(
            query = "",
            onQueryChange = {},
            idiomaAprendizaje = "Miskito",
            onBuscar = {},
            isLoading = false,
            errorMessage = null,
            resultados = resultadosDeMuestra,
            favoritosIds = emptySet(),
            onAlternarFavorito = {},
            onAbrirDetalle = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun DictionaryScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        DictionaryScreenContenido(
            query = "a",
            onQueryChange = {},
            idiomaAprendizaje = "Miskito",
            onBuscar = {},
            isLoading = true,
            errorMessage = null,
            resultados = emptyList(),
            favoritosIds = emptySet(),
            onAlternarFavorito = {},
            onAbrirDetalle = {}
        )
    }
}

@Preview(showBackground = true, name = "Sin resultados")
@Composable
private fun DictionaryScreenContenidoVacioPreview() {
    AikukisnaTheme {
        DictionaryScreenContenido(
            query = "zzz",
            onQueryChange = {},
            idiomaAprendizaje = "Miskito",
            onBuscar = {},
            isLoading = false,
            errorMessage = null,
            resultados = emptyList(),
            favoritosIds = emptySet(),
            onAlternarFavorito = {},
            onAbrirDetalle = {}
        )
    }
}
