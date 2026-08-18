package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aikukisna.app.presentacion.componentes.CardProgreso
import com.aikukisna.app.presentacion.componentes.NavBar
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onCerrarSesion: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavBar(
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onCerrarSesion = onCerrarSesion
                )
                1 -> CardProgreso(uiState = ProgresoUiState.Cargando)
                2 -> Text("Cultura e Historias")
                3 -> Text("Diccionario de Palabras")
                4 -> Text("Configuración de Perfil")
            }
        }
    }
}