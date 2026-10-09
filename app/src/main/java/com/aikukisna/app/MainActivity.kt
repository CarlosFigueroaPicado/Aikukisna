package com.aikukisna.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.aikukisna.app.presentacion.navegacion.GrafoNavegacion
import com.aikukisna.app.presentacion.pantallas.SplashScreen
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: SupabaseClient
    @Inject lateinit var authRepository: AuthRepository
    private val recuperacionContrasena = MutableStateFlow(false)

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.aikukisna.app.presentacion.idioma.IdiomaInterfaz.envolver(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            AikukisnaTheme {
                var mostrarSplash by remember { mutableStateOf(true) }
                var estaAutenticado by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    estaAutenticado = authRepository.usuarioActualId() != null
                }

                if (
                    mostrarSplash ||
                    estaAutenticado == null
                ) {
                    SplashScreen(onSplashFinished = { mostrarSplash = false })
                } else {
                    val abrirRecuperacion by recuperacionContrasena.collectAsState()
                    GrafoNavegacion(
                        estaAutenticado = estaAutenticado == true,
                        abrirNuevaContrasena = abrirRecuperacion,
                        onNuevaContrasenaAbierta = { recuperacionContrasena.value = false }
                    )
                }
            }
        }
        procesarEnlaceRecuperacion(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarEnlaceRecuperacion(intent)
    }

    private fun procesarEnlaceRecuperacion(intent: Intent) {
        if (intent.data?.path != "/recovery") return
        lifecycleScope.launch {
            supabaseClient.handleDeeplinks(
                intent = intent,
                onSessionSuccess = { recuperacionContrasena.value = true },
                onError = { recuperacionContrasena.value = false }
            )
        }
    }
}
