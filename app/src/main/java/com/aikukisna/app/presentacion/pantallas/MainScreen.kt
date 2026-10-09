package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.presentacion.componentes.NavBar
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onCerrarSesion: () -> Unit,
    onAbrirLeccion: (Int) -> Unit = {},
    onCambiarIdioma: () -> Unit = {},
    onChatIA: () -> Unit = {},
    onTraductor: () -> Unit = {},
    onCamara: () -> Unit = {},
    onLogros: () -> Unit = {},
    onRepaso: () -> Unit = {},
    onAbrirDetallePalabra: (Int) -> Unit = {},
    onVerFavoritos: () -> Unit = {},
    onConfiguracion: () -> Unit = {},
    onCultura: () -> Unit = {},
    onEditarPerfil: () -> Unit = {}
) {
    // Saveable: al volver del detalle de una palabra se sigue en el Diccionario, con la búsqueda hecha.
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    MainScreenContenido(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) { tab ->
        when (tab) {
            0 -> HomeScreen(
                viewModel = homeViewModel,
                onCerrarSesion = onCerrarSesion,
                onCambiarIdioma = onCambiarIdioma,
                onContinuarLeccion = onAbrirLeccion,
                onChatIA = onChatIA,
                onTraductor = onTraductor,
                onCamara = onCamara,
                onLogros = onLogros,
                onRepaso = onRepaso
            )
            1 -> LeccionesScreen(onAbrirLeccion = onAbrirLeccion)
            2 -> DictionaryScreen(
                viewModel = hiltViewModel(),
                onAbrirDetalle = onAbrirDetallePalabra
            )
            3 -> PerfilScreen(
                onVerLogros = onLogros,
                onVerFavoritos = onVerFavoritos,
                onConfiguracion = onConfiguracion,
                onCultura = onCultura,
                onEditarPerfil = onEditarPerfil
            )
        }
    }
}
@Composable
private fun MainScreenContenido(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    contenido: @Composable (tab: Int) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavBar(
                selectedIndex = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            contenido(selectedTab)
        }
    }
}
