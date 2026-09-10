package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aikukisna.app.presentacion.viewmodel.FavoritosViewModel

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel = hiltViewModel(),
    onAbrirDetalle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Favoritos", style = MaterialTheme.typography.titleSmall)
            Text("Volver", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onVolver))
        }
        Spacer(Modifier.height(20.dp))
        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            viewModel.errorMessage != null -> Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
            viewModel.favoritos.isEmpty() -> Text("Todavía no tienes palabras favoritas.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.favoritos, key = { it.palabra.id }) { favorito ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onAbrirDetalle(favorito.palabra.id) }.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(favorito.palabra.texto, style = MaterialTheme.typography.titleMedium)
                            Text(favorito.palabra.idioma.nombre, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Quitar", color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { viewModel.quitar(favorito.palabra.id) })
                    }
                }
            }
        }
    }
}
