package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel
) {
    DictionaryScreenContenido(
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        resultados = viewModel.resultados
    )
}

@Composable
private fun DictionaryScreenContenido(
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    resultados: List<PalabraConTraduccion>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Diccionario",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        AikukisnaTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "Buscar en Miskito",
            style = InputStyle.Outlined,
            leadingIcon = R.drawable.search
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(resultados) { item ->
                        PalabraItem(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun PalabraItem(item: PalabraConTraduccion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.texto,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.traduccion != null) {
                        Text(
                            text = "  •  ${item.traduccion}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
            resultados = resultadosDeMuestra
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
            resultados = emptyList()
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
            resultados = emptyList()
        )
    }
}