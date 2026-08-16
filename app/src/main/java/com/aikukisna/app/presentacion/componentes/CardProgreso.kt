package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.presentacion.pantallas.ProgresoUiState

@Composable
fun CardProgreso(
    uiState: ProgresoUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Column {
                when (uiState) {
                    is ProgresoUiState.Cargando -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ProgresoUiState.Exito -> {
                        if (uiState.listaProgreso.isEmpty()) {
                            Text(
                                text = "No hay progreso",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "Progreso",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            uiState.listaProgreso.forEach { progreso ->
                                ItemProgresoLeccion(leccion = progreso)
                            }
                        }
                    }

                    is ProgresoUiState.Error -> {
                        Text(
                            text = uiState.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemProgresoLeccion(
    leccion: ProgresoLeccion
){
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
              text = leccion.toString(),
              style = MaterialTheme.typography.bodyMedium
          )
        }
    }
}

@Preview
@Composable
fun PreviewCardProgreso() {
    CardProgreso( ProgresoUiState.Cargando)
}
