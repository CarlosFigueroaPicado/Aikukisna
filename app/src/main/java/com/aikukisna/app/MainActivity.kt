package com.aikukisna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aikukisna.app.data.repository.AuthRepositoryImpl
import com.aikukisna.app.data.repository.UsuarioRepositoryImpl
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import com.aikukisna.app.presentacion.navegacion.GrafoNavegacion
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }

        val authRepo = AuthRepositoryImpl(supabase)
        val userRepo = UsuarioRepositoryImpl(supabase)

        val loginVm = LoginViewModel(IniciarSesionUseCase(authRepo))
        val homeVm = HomeViewModel(userRepo, authRepo)

        setContent {
            AikukisnaTheme {
                var estaAutenticado by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    estaAutenticado = authRepo.usuarioActualId() != null
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (estaAutenticado != null) {
                        GrafoNavegacion(
                            estaAutenticado = estaAutenticado!!,
                            homeViewModel = homeVm,
                            loginViewModel = loginVm
                        )
                    }
                }
            }
        }
    }
}


