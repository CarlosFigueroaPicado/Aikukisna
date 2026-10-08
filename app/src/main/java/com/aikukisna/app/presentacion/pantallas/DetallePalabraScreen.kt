package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aikukisna.app.presentacion.viewmodel.DetallePalabraViewModel

@Composable
fun DetallePalabraScreen(
    palabraId: Int,
    viewModel: DetallePalabraViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    LaunchedEffect(palabraId) { viewModel.cargar(palabraId) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Detalle", style = MaterialTheme.typography.titleSmall)
            Text("Volver", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onVolver))
        }
        Spacer(Modifier.height(24.dp))
        when {
            viewModel.isLoading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator() }
            viewModel.errorMessage != null -> Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
            viewModel.detalle != null -> {
                val detalle = viewModel.detalle!!
                Text(detalle.palabra.texto, style = MaterialTheme.typography.displaySmall)
                Text(detalle.palabra.idioma.nombre, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (viewModel.esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp).clickable { viewModel.cambiarFavorito() }
                )
                Spacer(Modifier.height(20.dp))
                Text("Traducciones", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detalle.traducciones) { traduccion ->
                        Text("${traduccion.palabraDestino.texto} (${traduccion.palabraDestino.idioma.nombre})")
                    }
                }
            }
        }
    }
}
