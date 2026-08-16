package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.presentacion.componentes.CardProgreso
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel


@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCerrarSesion: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                HomeUiState.Cargando -> {
                    CircularProgressIndicator()
                }
                is HomeUiState.Exito -> {
                    HomeContenido(usuario = s.usuario)
                }
                is HomeUiState.Error -> {
                    Text(
                        text = s.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContenido(usuario: Usuario) {
    val nombreMostrar = usuario.nombre
        ?: usuario.nombreUsuario
        ?: "Estudiante"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Saludo
        Text(
            text = "¡Hola, $nombreMostrar! 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        usuario.idiomaMeta?.let { idioma ->
            Text(
                text = "Aprendiendo ${idioma.nombre}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        CardProgreso( uiState = ProgresoUiState.Exito(listaProgreso = emptyList()))

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Continúa tu aprendizaje",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
