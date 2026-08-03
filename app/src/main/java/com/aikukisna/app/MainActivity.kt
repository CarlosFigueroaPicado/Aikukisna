package com.aikukisna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aikukisna.app.domain.usecase.ObtenerLeccionesUseCase
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Prueba de humo temporal: confirma que Hilt + data + Supabase real
// funcionan de punta a punta. Se reemplaza cuando `presentation` conecte
// la UI definitiva.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var obtenerLeccionesUseCase: ObtenerLeccionesUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AikukisnaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var mensaje by remember { mutableStateOf("Cargando lecciones desde Supabase...") }

                    LaunchedEffect(Unit) {
                        mensaje = try {
                            val lecciones = obtenerLeccionesUseCase()
                            "Conexión OK: se cargaron ${lecciones.size} lecciones desde Supabase."
                        } catch (e: Exception) {
                            "Error al conectar: ${e.message}"
                        }
                    }

                    Text(text = mensaje, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}