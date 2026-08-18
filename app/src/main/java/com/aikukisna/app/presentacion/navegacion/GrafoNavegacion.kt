// presentacion/navegacion/GrafoNavegacion.kt
package com.aikukisna.app.presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aikukisna.app.presentacion.pantallas.LoginScreen
import com.aikukisna.app.presentacion.pantallas.MainScreen
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel

sealed class Destinos(val ruta: String) {
    object Login : Destinos("login_screen")
    object Main : Destinos("main_screen")
}

@Composable
fun GrafoNavegacion(
    estaAutenticado: Boolean,
    homeViewModel: HomeViewModel,
    loginViewModel: com.aikukisna.app.presentacion.viewmodel.LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val inicio = if (estaAutenticado) Destinos.Main.ruta else Destinos.Login.ruta

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        // PANTALLA LOGIN
        composable(Destinos.Login.ruta) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA PRINCIPAL CON BOTTOMBAR
        composable(Destinos.Main.ruta) {
            MainScreen(
                homeViewModel = homeViewModel,
                onCerrarSesion = {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Main.ruta) { inclusive = true }
                    }
                }
            )
        }
    }
}