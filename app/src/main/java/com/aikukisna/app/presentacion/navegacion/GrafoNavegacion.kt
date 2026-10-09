package com.aikukisna.app.presentacion.navegacion

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.usecase.ObtenerPalabrasDemoUseCase
import com.aikukisna.app.presentacion.pantallas.CamaraScreen
import com.aikukisna.app.presentacion.pantallas.CompletarPerfilScreen
import com.aikukisna.app.presentacion.pantallas.ConfiguracionScreen
import com.aikukisna.app.presentacion.pantallas.CulturaScreen
import com.aikukisna.app.presentacion.pantallas.DescargaOfflineScreen
import com.aikukisna.app.presentacion.pantallas.DetallePalabraScreen
import com.aikukisna.app.presentacion.pantallas.FavoritosScreen
import com.aikukisna.app.presentacion.pantallas.GuestModeIntroScreen
import com.aikukisna.app.presentacion.pantallas.GuestModeResultsScreen
import com.aikukisna.app.presentacion.pantallas.GuestQuizScreen
import com.aikukisna.app.presentacion.pantallas.GuestVocabularioScreen
import com.aikukisna.app.presentacion.pantallas.LeccionQuizScreen
import com.aikukisna.app.presentacion.pantallas.LeccionInicioQuizScreen
import com.aikukisna.app.presentacion.pantallas.LeccionResultadosScreen
import com.aikukisna.app.presentacion.pantallas.LeccionVocabularioScreen
import com.aikukisna.app.presentacion.pantallas.LoginScreen
import com.aikukisna.app.presentacion.pantallas.LogrosScreen
import com.aikukisna.app.presentacion.pantallas.MainScreen
import com.aikukisna.app.presentacion.pantallas.NuevaContrasenaScreen
import com.aikukisna.app.presentacion.pantallas.OnboardingScreen
import com.aikukisna.app.presentacion.pantallas.RecuperarContrasenaScreen
import com.aikukisna.app.presentacion.pantallas.RegisterScreen
import com.aikukisna.app.presentacion.pantallas.SeleccionarIdiomaScreen
import com.aikukisna.app.presentacion.pantallas.TraductorScreen
import com.aikukisna.app.presentacion.pantallas.TukiScreen
import com.aikukisna.app.presentacion.pantallas.DescargaModeloScreen
import com.aikukisna.app.presentacion.pantallas.GuestInicioQuizScreen
import com.aikukisna.app.presentacion.pantallas.TerminosScreen
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.PasoPrimerUso
import com.aikukisna.app.presentacion.viewmodel.PrimerUsoViewModel
import com.aikukisna.app.presentacion.viewmodel.DescargaOfflineViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

/**
 * "Volver" que nunca saca la última pantalla de la pila. Un doble toque rápido en la flecha
 * (o un espacio de teclado sobre el botón enfocado) dejaba la navegación vacía: pantalla en blanco.
 */
fun NavController.volverSeguro() {
    if (previousBackStackEntry != null) popBackStack()
}

sealed class Destinos(val ruta: String) {
    object Onboarding : Destinos("onboarding_screen")
    object Register : Destinos("register_screen")
    object Login : Destinos("login_screen")
    object RecuperarContrasena : Destinos("recuperar_contrasena_screen")
    object NuevaContrasena : Destinos("nueva_contrasena_screen")
    object SeleccionarIdioma : Destinos("seleccionar_idioma_screen")
    object GuestModeIntro : Destinos("guest_mode_intro_screen")
    object GuestVocabulario : Destinos("guest_vocabulario_screen")
    object GuestQuiz : Destinos("guest_quiz_screen")
    object GuestModeResults : Destinos("guest_mode_results_screen")
    object Main : Destinos("main_screen")
    object DescargaOffline : Destinos("descarga_offline_screen")
    object Terminos : Destinos("terminos_screen")
    object DescargaModelo : Destinos("descarga_modelo_screen")
    object GuestInicioQuiz : Destinos("guest_inicio_quiz_screen")

    object SeleccionarIdiomaRegistro : Destinos("seleccionar_idioma_registro_screen")
    object SeleccionarIdiomaLogin : Destinos("seleccionar_idioma_login_screen")
    object CambiarIdioma : Destinos("cambiar_idioma_screen")
    object Tuki : Destinos("tuki_screen")
    object Traductor : Destinos("traductor_screen")
    object Camara : Destinos("camara_screen")
    object Logros : Destinos("logros_screen")
    object Favoritos : Destinos("favoritos_screen")
    object Cultura : Destinos("cultura_screen")
    object Configuracion : Destinos("configuracion_screen")
    object CompletarPerfil : Destinos("completar_perfil_screen")
    object DetallePalabra : Destinos("detalle_palabra_screen/{palabraId}") {
        fun crearRuta(palabraId: Int) = "detalle_palabra_screen/${palabraId}"
    }

    object LeccionVocabulario : Destinos("leccion_vocabulario_screen/{leccionId}") {
        fun crearRuta(leccionId: Int) = "leccion_vocabulario_screen/${leccionId}"
    }
    object LeccionQuiz : Destinos("leccion_quiz_screen/{leccionId}") {
        fun crearRuta(leccionId: Int) = "leccion_quiz_screen/${leccionId}"
    }
    object LeccionInicioQuiz : Destinos("leccion_inicio_quiz_screen/{leccionId}") {
        fun crearRuta(leccionId: Int) = "leccion_inicio_quiz_screen/${leccionId}"
    }
    object LeccionResultados : Destinos("leccion_resultados_screen/{correctas}/{total}/{palabras}/{aprobado}/{xp}") {
        fun crearRuta(correctas: Int, total: Int, palabras: Int, aprobado: Boolean, xp: Int) =
            "leccion_resultados_screen/$correctas/$total/$palabras/$aprobado/$xp"
    }
}

@Composable
fun GrafoNavegacion(
    estaAutenticado: Boolean,
    abrirNuevaContrasena: Boolean = false,
    onNuevaContrasenaAbierta: () -> Unit = {}
) {
    val navController = rememberNavController()

    var idiomaInvitado by remember { mutableStateOf<Idioma?>(null) }

    var resultadoQuizInvitado by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Tras autenticarse, el usuario pasa una sola vez por los términos y la descarga del modelo.
    val primerUso: PrimerUsoViewModel = hiltViewModel()
    fun rutaTrasAutenticar(): String = when (primerUso.pasoPendiente()) {
        PasoPrimerUso.TERMINOS -> Destinos.Terminos.ruta
        PasoPrimerUso.DESCARGA_MODELO -> Destinos.DescargaModelo.ruta
        PasoPrimerUso.NINGUNO -> Destinos.Main.ruta
    }

    val inicio = remember(estaAutenticado) {
        when {
            estaAutenticado -> rutaTrasAutenticar()
            primerUso.onboardingVisto() -> Destinos.Login.ruta
            else -> Destinos.Onboarding.ruta
        }
    }

    LaunchedEffect(abrirNuevaContrasena) {
        if (abrirNuevaContrasena) {
            navController.navigate(Destinos.NuevaContrasena.ruta) {
                launchSingleTop = true
            }
            onNuevaContrasenaAbierta()
        }
    }

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {
        composable(Destinos.Onboarding.ruta) {
            OnboardingScreen(
                onOnboardingTerminado = {
                    primerUso.marcarOnboardingVisto()
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(Destinos.Onboarding.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.Register.ruta) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onRegistroExitoso = {
                    navController.navigate(rutaTrasAutenticar()) {
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

        composable(Destinos.SeleccionarIdiomaRegistro.ruta) { backStackEntry ->
            val registerEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinos.Register.ruta)
            }
            val registerViewModel: RegisterViewModel = hiltViewModel(registerEntry)
            LaunchedEffect(registerViewModel.registroExitoso) {
                if (registerViewModel.registroExitoso) {
                    navController.navigate(rutaTrasAutenticar()) {
                        popUpTo(Destinos.Register.ruta) { inclusive = true }
                    }
                }
            }
            SeleccionarIdiomaScreen(
                onContinuar = registerViewModel::continuarConIdioma,
                isLoading = registerViewModel.isLoading,
                errorMessage = registerViewModel.errorMessage
            )
        }

        composable(Destinos.Login.ruta) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(rutaTrasAutenticar()) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                },
                onSeleccionarIdioma = {
                    navController.navigate(Destinos.SeleccionarIdiomaLogin.ruta)
                },
                onIrARegistro = {
                    navController.navigate(Destinos.Register.ruta) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                },
                onEntrarComoInvitado = {
                    navController.navigate(Destinos.SeleccionarIdioma.ruta)
                },
                onRecuperarContrasena = {
                    navController.navigate(Destinos.RecuperarContrasena.ruta)
                }
            )
        }

        composable(Destinos.RecuperarContrasena.ruta) {
            RecuperarContrasenaScreen(
                viewModel = hiltViewModel(),
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(Destinos.NuevaContrasena.ruta) {
            NuevaContrasenaScreen(
                viewModel = hiltViewModel(),
                onCompletado = {
                    navController.navigate(Destinos.Login.ruta) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.SeleccionarIdiomaLogin.ruta) { backStackEntry ->
            val loginEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinos.Login.ruta)
            }
            val loginViewModel: LoginViewModel = hiltViewModel(loginEntry)
            LaunchedEffect(loginViewModel.loginExitoso) {
                if (loginViewModel.loginExitoso) {
                    navController.navigate(rutaTrasAutenticar()) {
                        popUpTo(Destinos.Login.ruta) { inclusive = true }
                    }
                }
            }
            SeleccionarIdiomaScreen(
                onContinuar = loginViewModel::confirmarIdiomaSeleccionado,
                isLoading = loginViewModel.isLoading,
                errorMessage = loginViewModel.errorMessage
            )
        }

        composable(Destinos.DescargaOffline.ruta) {
            val viewModel: DescargaOfflineViewModel = hiltViewModel()
            LaunchedEffect(viewModel.lista) {
                if (viewModel.lista) {
                    navController.navigate(rutaTrasAutenticar()) {
                        popUpTo(Destinos.DescargaOffline.ruta) { inclusive = true }
                    }
                }
            }
            DescargaOfflineScreen(
                estado = viewModel.estado,
                progresoAudio = viewModel.progresoAudio,
                audioActivo = viewModel.audioActivo,
                onReintentar = viewModel::reintentar
            )
        }

        composable(Destinos.Terminos.ruta) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            TerminosScreen(
                onAceptar = {
                    primerUso.aceptarTerminos()
                    navController.navigate(rutaTrasAutenticar()) {
                        popUpTo(Destinos.Terminos.ruta) { inclusive = true }
                    }
                },
                onRechazar = {
                    homeViewModel.cerrarSesion {
                        navController.navigate(Destinos.Login.ruta) {
                            popUpTo(Destinos.Terminos.ruta) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Destinos.DescargaModelo.ruta) {
            DescargaModeloScreen(
                onContinuar = {
                    navController.navigate(Destinos.Main.ruta) {
                        popUpTo(Destinos.DescargaModelo.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.SeleccionarIdioma.ruta) {
            SeleccionarIdiomaScreen(
                onContinuar = { idioma ->
                    idiomaInvitado = idioma
                    navController.navigate(Destinos.GuestModeIntro.ruta)
                },
                subtitulo = t(R.string.grafonavegacion_elige_uno_para_tu_leccion)
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
                    onVolver = { navController.volverSeguro() }
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
                        navController.navigate(Destinos.GuestInicioQuiz.ruta)
                    },
                    onVolver = { navController.volverSeguro() }
                )
            }
        }

        composable(Destinos.GuestInicioQuiz.ruta) {
            GuestInicioQuizScreen(
                onContinuar = {
                    navController.navigate(Destinos.GuestQuiz.ruta) {
                        popUpTo(Destinos.GuestInicioQuiz.ruta) { inclusive = true }
                    }
                },
                onVolver = { navController.volverSeguro() }
            )
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
                        // Atrás desde los resultados vuelve al inicio, no al quiz ya terminado.
                        navController.navigate(Destinos.GuestModeResults.ruta) {
                            popUpTo(Destinos.SeleccionarIdioma.ruta) { inclusive = true }
                        }
                    },
                    onVolver = { navController.volverSeguro() }
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
            val homeViewModel: HomeViewModel = hiltViewModel()
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
                    navController.navigate(Destinos.CambiarIdioma.ruta) { launchSingleTop = true }
                },
                onChatIA = {
                    navController.navigate(Destinos.Tuki.ruta)
                },
                onTraductor = {
                    navController.navigate(Destinos.Traductor.ruta)
                },
                onCamara = {
                    navController.navigate(Destinos.Camara.ruta)
                },
                onLogros = {
                    navController.navigate(Destinos.Logros.ruta)
                },
                onAbrirDetallePalabra = { palabraId ->
                    navController.navigate(Destinos.DetallePalabra.crearRuta(palabraId))
                },
                onVerFavoritos = {
                    navController.navigate(Destinos.Favoritos.ruta)
                },
                onConfiguracion = {
                    navController.navigate(Destinos.Configuracion.ruta)
                },
                onCultura = {
                    navController.navigate(Destinos.Cultura.ruta)
                },
                onEditarPerfil = {
                    navController.navigate(Destinos.CompletarPerfil.ruta)
                }
            )
        }

        composable(Destinos.CambiarIdioma.ruta) { backStackEntry ->
            val mainEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinos.Main.ruta)
            }
            val homeViewModel: HomeViewModel = hiltViewModel(mainEntry)
            SeleccionarIdiomaScreen(
                onContinuar = { idioma ->
                    homeViewModel.cambiarIdioma(idioma)
                    navController.volverSeguro()
                },
                subtitulo = t(R.string.grafonavegacion_tu_progreso_en_cada_idioma)
            )
        }

        composable(Destinos.Tuki.ruta) {
            TukiScreen(onVolver = { navController.volverSeguro() })
        }

        composable(Destinos.Traductor.ruta) {
            TraductorScreen(onVolver = { navController.volverSeguro() })
        }

        composable(Destinos.Camara.ruta) {
            CamaraScreen(onVolver = { navController.volverSeguro() })
        }

        composable(Destinos.Logros.ruta) {
            LogrosScreen(onVolver = { navController.volverSeguro() })
        }

        composable(Destinos.Favoritos.ruta) {
            FavoritosScreen(
                onAbrirDetalle = { palabraId ->
                    navController.navigate(Destinos.DetallePalabra.crearRuta(palabraId))
                },
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(Destinos.Cultura.ruta) {
            CulturaScreen(onVolver = { navController.volverSeguro() })
        }

        composable(Destinos.Configuracion.ruta) { backStackEntry ->
            val mainEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinos.Main.ruta)
            }
            val homeViewModel: HomeViewModel = hiltViewModel(mainEntry)
            ConfiguracionScreen(
                onCambiarIdioma = { navController.navigate(Destinos.CambiarIdioma.ruta) { launchSingleTop = true } },
                onCerrarSesion = {
                    homeViewModel.cerrarSesion {
                        navController.navigate(Destinos.Login.ruta) {
                            popUpTo(Destinos.Main.ruta) { inclusive = true }
                        }
                    }
                },
                onVolver = { navController.volverSeguro() },
                idiomaActual = (homeViewModel.uiState.collectAsState().value as? com.aikukisna.app.presentacion.pantallas.HomeUiState.Exito)
                    ?.usuario?.idiomaMeta?.nombre
            )
        }

        composable(Destinos.CompletarPerfil.ruta) {
            CompletarPerfilScreen(
                onGuardado = { navController.popBackStack(Destinos.Main.ruta, inclusive = false) },
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(
            route = Destinos.DetallePalabra.ruta,
            arguments = listOf(navArgument("palabraId") { type = NavType.IntType })
        ) { backStackEntry ->
            val palabraId = backStackEntry.arguments?.getInt("palabraId") ?: return@composable
            DetallePalabraScreen(
                palabraId = palabraId,
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(
            route = Destinos.LeccionVocabulario.ruta,
            arguments = listOf(navArgument("leccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leccionId = backStackEntry.arguments?.getInt("leccionId") ?: return@composable
            LeccionVocabularioScreen(
                leccionId = leccionId,
                onCompletado = {
                    navController.navigate(Destinos.LeccionInicioQuiz.crearRuta(leccionId))
                },
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(
            route = Destinos.LeccionInicioQuiz.ruta,
            arguments = listOf(navArgument("leccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leccionId = backStackEntry.arguments?.getInt("leccionId") ?: return@composable
            LeccionInicioQuizScreen(
                leccionId = leccionId,
                onContinuar = {
                    navController.navigate(Destinos.LeccionQuiz.crearRuta(leccionId)) {
                        popUpTo(Destinos.LeccionInicioQuiz.ruta) { inclusive = true }
                    }
                },
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(
            route = Destinos.LeccionQuiz.ruta,
            arguments = listOf(navArgument("leccionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leccionId = backStackEntry.arguments?.getInt("leccionId") ?: return@composable
            LeccionQuizScreen(
                leccionId = leccionId,
                onCompletado = { correctas, total, palabras, aprobado, xp ->
                    navController.navigate(Destinos.LeccionResultados.crearRuta(correctas, total, palabras, aprobado, xp)) {
                        popUpTo(Destinos.Main.ruta)
                    }
                },
                onVolver = { navController.volverSeguro() }
            )
        }

        composable(
            route = Destinos.LeccionResultados.ruta,
            arguments = listOf(
                navArgument("correctas") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("palabras") { type = NavType.IntType },
                navArgument("aprobado") { type = NavType.BoolType },
                navArgument("xp") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val correctas = backStackEntry.arguments?.getInt("correctas") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val palabras = backStackEntry.arguments?.getInt("palabras") ?: 0
            val aprobado = backStackEntry.arguments?.getBoolean("aprobado") ?: false
            val mainEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry(Destinos.Main.ruta) }.getOrNull()
            }
            val idiomaId = mainEntry?.let { entry ->
                val homeViewModel: HomeViewModel = hiltViewModel(entry)
                (homeViewModel.uiState.collectAsState().value as? com.aikukisna.app.presentacion.pantallas.HomeUiState.Exito)
                    ?.usuario?.idiomaMeta?.id
            }
            LeccionResultadosScreen(
                respuestasCorrectas = correctas,
                totalPreguntas = total,
                palabrasAprendidas = palabras,
                aprobado = aprobado,
                xpGanado = backStackEntry.arguments?.getInt("xp") ?: 0,
                onVolver = { navController.volverSeguro() },
                idiomaId = idiomaId
            )
        }
    }
}
