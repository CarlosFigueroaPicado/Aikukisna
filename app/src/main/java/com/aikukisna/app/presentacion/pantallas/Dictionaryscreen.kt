package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.InputStyle

import com.aikukisna.app.presentacion.viewmodel.DictionaryViewModel
import com.aikukisna.app.presentacion.viewmodel.PalabraConTraduccion
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.OrangePrimary

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel,
    onAbrirDetalle: (Int) -> Unit = {}
) {
    DictionaryScreenContenido(
        query = viewModel.query,
        idiomaNombre = viewModel.idiomaNombre,
        onQueryChange = viewModel::onQueryChange,
        isLoading = viewModel.isLoading,
        cargandoMas = viewModel.cargandoMas,
        hayMasResultados = viewModel.hayMasResultados,
        errorMessage = viewModel.errorMessage,
        resultados = viewModel.resultados,
        onAbrirDetalle = onAbrirDetalle,
        onCargarMas = viewModel::cargarMas
    )
}

@Composable
private fun DictionaryScreenContenido(
    query: String,
    idiomaNombre: String = "tu idioma",
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    cargandoMas: Boolean = false,
    hayMasResultados: Boolean = true,
    errorMessage: String?,
    resultados: List<PalabraConTraduccion>,
    onAbrirDetalle: (Int) -> Unit,
    onCargarMas: () -> Unit = {}
) {
    val listaState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(text = "Diccionario", style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground)
        Text(text = "Explora palabras en $idiomaNombre", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(14.dp))

        AikukisnaTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Buscar una palabra",
            style = InputStyle.Outlined,
            leadingIcon = R.drawable.search
        )

        Spacer(modifier = Modifier.height(12.dp))
        FilterChip(
            selected = true,
            onClick = {},
            label = { Text("Todas las palabras") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrandSubtle,
                selectedLabelColor = OrangePrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = true,
                borderColor = OrangePrimary
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        when {
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
            resultados.isEmpty() -> {
                Text(
                    text = "Sin resultados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    state = listaState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(resultados) { item ->
                        PalabraItem(item, onClick = { if (item.id != 0) onAbrirDetalle(item.id) })
                    }
                    if (hayMasResultados) {
                        item {
                            if (cargandoMas) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LaunchedEffect(resultados.size) {
                                    onCargarMas()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PalabraItem(item: PalabraConTraduccion, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (item.id != 0) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.texto,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(text = item.traduccion ?: "Sin traducción disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.VolumeUp, contentDescription = "Escuchar pronunciación",
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Ver palabra",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            isLoading = false,
            errorMessage = null,
            resultados = resultadosDeMuestra,
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
            isLoading = true,
            errorMessage = null,
            resultados = emptyList(), onAbrirDetalle = {}
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
            isLoading = false,
            errorMessage = null,
            resultados = emptyList(), onAbrirDetalle = {}
        )
    }
}
