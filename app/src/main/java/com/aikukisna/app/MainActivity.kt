package com.aikukisna.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.aikukisna.app.presentacion.navegacion.GrafoNavegacion
import com.aikukisna.app.presentacion.pantallas.SplashScreen
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val estaAutenticado = supabaseClient.auth.currentSessionOrNull() != null

        setContent {
            AikukisnaTheme {
                var mostrarSplash by remember { mutableStateOf(true) }

                if (mostrarSplash) {
                    SplashScreen(onSplashFinished = { mostrarSplash = false })
                } else {
                    GrafoNavegacion(estaAutenticado = estaAutenticado)
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            supabaseClient.handleDeeplinks(intent)
        }
    }
}