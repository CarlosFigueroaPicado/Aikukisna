package com.aikukisna.app.presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.usecase.ObtenerPalabrasDemoUseCase
import com.aikukisna.app.presentacion.pantallas.CamaraScreen
import com.aikukisna.app.presentacion.pantallas.GuestModeIntroScreen
import com.aikukisna.app.presentacion.pantallas.GuestModeResultsScreen
import com.aikukisna.app.presentacion.pantallas.GuestQuizScreen
import com.aikukisna.app.presentacion.pantallas.GuestVocabularioScreen
import com.aikukisna.app.presentacion.pantallas.LeccionQuizScreen
import com.aikukisna.app.presentacion.pantallas.LeccionResultadosScreen
import com.aikukisna.app.presentacion.pantallas.LeccionVocabularioScreen
import com.aikukisna.app.presentacion.pantallas.LoginScreen
import com.aikukisna.app.presentacion.pantallas.MainScreen
import com.aikukisna.app.presentacion.pantallas.OnboardingScreen
import com.aikukisna.app.presentacion.pantallas.RegisterScreen
import com.aikukisna.app.presentacion.pantallas.SeleccionarIdiomaScreen
import com.aikukisna.app.presentacion.pantallas.TraductorScreen
import com.aikukisna.app.presentacion.pantallas.TukiScreen
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

sealed class Destinos(val ruta: String) {
    object Onboarding : Destinos("onboarding_screen")
    object Register : Destinos("register_screen")
    object Login : Destinos("login_screen")
    object SeleccionarIdioma : Destinos("seleccionar_idioma_screen")
    object GuestModeIntro : Destinos("guest_mode_intro_screen")
    object GuestVocabulario : Destinos("guest_vocabulario_screen")
    object GuestQuiz : Destinos("guest_quiz_screen")
    object GuestModeResults : Destinos("guest_mode_results_screen")
    object Main : Destinos("main_screen")

    object SeleccionarIdiomaRegistro : Destinos("seleccionar_idioma_registro_screen")
    object CambiarIdioma : Destinos("cambiar_idioma_screen")
    object Tuki : Destinos("tuki_screen")
    object Traductor : Destinos("traductor_screen")
    object Camara : Destinos("camara_screen")

    object LeccionVocabulario : Destinos("leccion_vocabulario_screen/{leccionId}") {
        fun crearRuta(leccionId: Int) = "leccion_vocabulario_screen/$leccionId"
    }
    object LeccionQuiz : Destinos("leccion_quiz_screen/{leccionId}") {
        fun crearRuta(leccionId: Int) = "leccion_quiz_screen/$leccionId"
    }
    object LeccionResultados : Destinos("leccion_resultados_screen/{correctas}/{total}") {
        fun crearRuta(correctas: Int, total: Int) = "leccion_resultados_screen/$correctas/$total"
    }
}

@Composable
fun GrafoNavegacion(
    estaAutenticado: Boolean,
    homeViewModel: HomeViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    var idiomaInvitado by remember { mutableStateOf<Idioma?>(null) }

    var resultadoQuizInvitado by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val inicio = if (estaAutenticado) Destinos.Main.ruta else Destinos.Onboarding.ruta

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Destinos.Onboarding.ruta) {
            OnboardingScreen(
                onOnboardingTerminado = {
                    navController.navigate(Destinos.Login.ruta) {
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
                onCamposValidos = {
                    navController.navigate(Destinos.SeleccionarIdiomaRegistro.ruta)
                },
                onIrALogin = {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Register.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.SeleccionarIdiomaRegistro.ruta) {
            LaunchedEffect(registerViewModel.registroExitoso) {
                if (registerViewModel.registroExitoso) {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.Register.ruta) { inclusive = true }
                    }
                }
            }
            SeleccionarIdiomaScreen(
                onContinuar = { idioma -> registerViewModel.registrarConIdioma(idioma) },
                isLoading = registerViewModel.isLoading,
                errorMessage = registerViewModel.errorMessage
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
                },
                onEntrarComoInvitado = {
                    navController.navigate(Destinos.SeleccionarIdioma.ruta)
                }
            )
        }

        composable(Destinos.SeleccionarIdioma.ruta) {
            SeleccionarIdiomaScreen(
                onContinuar = { idioma ->
                    idiomaInvitado = idioma
                    navController.navigate(Destinos.GuestModeIntro.ruta)
                }
            )
        }

        composable(Destinos.GuestModeIntro.ruta) {
            val idioma = idiomaInvitado
            if (idioma == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            } else {
                GuestModeIntroScreen(
                    idioma = idioma,
                    onEmpezarLeccion = {
                        navController.navigate(Destinos.GuestVocabulario.ruta)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Destinos.GuestVocabulario.ruta) {
            val idioma = idiomaInvitado
            if (idioma == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            } else {
                GuestVocabularioScreen(
                    idioma = idioma,
                    onCompletado = {
                        navController.navigate(Destinos.GuestQuiz.ruta)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Destinos.GuestQuiz.ruta) {
            val idioma = idiomaInvitado
            if (idioma == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            } else {
                GuestQuizScreen(
                    idioma = idioma,
                    onCompletado = { correctas, total ->
                        resultadoQuizInvitado = correctas to total
                        navController.navigate(Destinos.GuestModeResults.ruta)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Destinos.GuestModeResults.ruta) {
            val idioma = idiomaInvitado
            val resultado = resultadoQuizInvitado
            if (idioma == null || resultado == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            } else {
                val (correctas, total) = resultado
                GuestModeResultsScreen(
                    idioma = idioma,
                    palabrasAprendidas = ObtenerPalabrasDemoUseCase.PALABRAS_DEMO,
                    respuestasCorrectas = correctas,
                    totalPreguntas = total,
                    onCrearCuenta = {
                        navController.navigate(Destinos.Register.ruta) {
                            popUpTo(Destinos.Login.ruta) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Destinos.Main.ruta) {
            MainScreen(
                homeViewModel = homeViewModel,
                onCerrarSesion = {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Main.ruta) { inclusive = true }
                    }
                },
                onAbrirLeccion = { leccionId ->
                    navController.navigate(Destinos.LeccionVocabulario.crearRuta(leccionId))
                },
                onCambiarIdioma = {
                    navController.navigate(Destinos.CambiarIdioma.ruta)
                },
                onChatIA = {
                    navController.navigate(Destinos.Tuki.ruta)
                },
                onTraductor = {
                    navController.navigate(Destinos.Traductor.ruta)
                },
                onCamara = {
                    navController.navigate(Destinos.Camara.ruta)
                }
            )
        }

        composable(Destinos.CambiarIdioma.ruta) {
            SeleccionarIdiomaScreen(
                onContinuar = { idioma ->
                    homeViewModel.cambiarIdioma(idioma)
                    navController.popBackStack()
                }
            )
        }

        composable(Destinos.Tuki.ruta) {
            TukiScreen(onVolver = { navController.popBackStack() })
        }

        composable(Destinos.Traductor.ruta) {
            TraductorScreen(onVolver = { navController.popBackStack() })
        }

        composable(Destinos.Camara.ruta) {
            CamaraScreen(onVolver = { navController.popBackStack() })
        }

        composable(
            route = Destinos.LeccionVocabulario.ruta,
            arguments = listOf(navArgument("leccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leccionId = backStackEntry.arguments?.getInt("leccionId") ?: return@composable
            LeccionVocabularioScreen(
                leccionId = leccionId,
                onCompletado = {
                    navController.navigate(Destinos.LeccionQuiz.crearRuta(leccionId))
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.LeccionQuiz.ruta,
            arguments = listOf(navArgument("leccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leccionId = backStackEntry.arguments?.getInt("leccionId") ?: return@composable
            LeccionQuizScreen(
                leccionId = leccionId,
                onCompletado = { correctas, total ->
                    navController.navigate(Destinos.LeccionResultados.crearRuta(correctas, total)) {
                        popUpTo(Destinos.Main.ruta)
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.LeccionResultados.ruta,
            arguments = listOf(
                navArgument("correctas") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val correctas = backStackEntry.arguments?.getInt("correctas") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            LeccionResultadosScreen(
                respuestasCorrectas = correctas,
                totalPreguntas = total,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}