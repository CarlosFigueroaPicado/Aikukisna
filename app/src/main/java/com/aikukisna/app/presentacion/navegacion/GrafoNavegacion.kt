package com.aikukisna.app.presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aikukisna.app.presentacion.pantallas.LoginScreen
import com.aikukisna.app.presentacion.pantallas.MainScreen
import com.aikukisna.app.presentacion.pantallas.RegisterScreen
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

import androidx.hilt.navigation.compose.hiltViewModel

sealed class Destinos(val ruta: String) {
    object Login : Destinos("login_screen")
    object Register : Destinos("register_screen")
    object Main : Destinos("main_screen")
}

@Composable
fun GrafoNavegacion(
    estaAutenticado: Boolean
) {
    val navController = rememberNavController()
    val inicio = if (estaAutenticado) Destinos.Main.ruta else Destinos.Login.ruta

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Destinos.Login.ruta) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Destinos.Register.ruta)
                }
            )
        }

        composable(Destinos.Register.ruta) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                    },
                onNavigateToLogin = {
                    navController.navigate(Destinos.Login.ruta)
                },
            )
        }

        composable(Destinos.Main.ruta) {
            val homeViewModel: HomeViewModel = hiltViewModel()
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
