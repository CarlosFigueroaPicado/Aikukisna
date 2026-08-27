// presentacion/navegacion/GrafoNavegacion.kt
package com.aikukisna.app.presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aikukisna.app.presentacion.pantallas.LoginScreen
import com.aikukisna.app.presentacion.pantallas.MainScreen
import com.aikukisna.app.presentacion.pantallas.OnboardingScreen
import com.aikukisna.app.presentacion.pantallas.RegisterScreen
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

sealed class Destinos(val ruta: String) {
    object Onboarding : Destinos("onboarding_screen")
    object Register : Destinos("register_screen")
    object Login : Destinos("login_screen")
    object Main : Destinos("main_screen")
}

@Composable
fun GrafoNavegacion(
    estaAutenticado: Boolean,
    homeViewModel: HomeViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val inicio = if (estaAutenticado) Destinos.Main.ruta else Destinos.Onboarding.ruta

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Destinos.Onboarding.ruta) {
            OnboardingScreen(
                onOnboardingTerminado = {
                    // Corregido: el diseño lleva de onboarding a Registro,
                    // no a Login directo (según las etiquetas del propio
                    // Figma: "onboarding, registrarse, modo invitado").
                    navController.navigate(Destinos.Register.ruta) {
                        popUpTo(Destinos.Onboarding.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.Register.ruta) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegistroExitoso = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Register.ruta) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Register.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.Login.ruta) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate(Destinos.Register.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

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