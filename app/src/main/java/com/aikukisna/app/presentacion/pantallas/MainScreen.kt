package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.presentacion.componentes.NavBar
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onCerrarSesion: () -> Unit,
    onAbrirLeccion: (Int) -> Unit = {},
    onCambiarIdioma: () -> Unit = {},
    onChatIA: () -> Unit = {},
    onTraductor: () -> Unit = {},
    onCamara: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    MainScreenContenido(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) { tab ->
        when (tab) {
            0 -> HomeScreen(
                viewModel = homeViewModel,
                onCerrarSesion = onCerrarSesion,
                onCambiarIdioma = onCambiarIdioma,
                onChatIA = onChatIA,
                onTraductor = onTraductor,
                onCamara = onCamara
            )
            1 -> LeccionesScreen(onAbrirLeccion = onAbrirLeccion)
            2 -> DictionaryScreen(viewModel = hiltViewModel())
            3 -> PantallaEnConstruccion("Perfil")
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

@Composable
private fun PantallaEnConstruccion(nombre: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$nombre — próximamente",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Pestaña Inicio (placeholder)")
@Composable
private fun MainScreenContenidoPreview() {
    AikukisnaTheme {
        MainScreenContenido(selectedTab = 0, onTabSelected = {}) { tab ->
            PantallaEnConstruccion("Tab $tab")
        }
    }
}

@Preview(showBackground = true, name = "Aprender / Perfil")
@Composable
private fun PantallaEnConstruccionPreview() {
    AikukisnaTheme {
        PantallaEnConstruccion("Perfil")
    }
}