package com.aikukisna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.presentacion.navegacion.GrafoNavegacion
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AikukisnaTheme {
                var estaAutenticado by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    estaAutenticado = authRepository.usuarioActualId() != null
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (estaAutenticado != null) {
                        GrafoNavegacion(estaAutenticado = estaAutenticado!!)
                    }
                }
            }
        }
    }
}
