package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.usecase.LogroConEstado
import com.aikukisna.app.domain.usecase.ObtenerFavoritosUseCase
import com.aikukisna.app.domain.usecase.ObtenerLogrosConEstadoUseCase
import com.aikukisna.app.domain.usecase.ObtenerProgresoUseCase
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private const val XP_POR_NIVEL = 500
private const val IDIOMA_ESPANOL_ID = 2

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val obtenerProgresoUseCase: ObtenerProgresoUseCase,
    private val obtenerLogrosConEstadoUseCase: ObtenerLogrosConEstadoUseCase,
    private val obtenerFavoritosUseCase: ObtenerFavoritosUseCase,
    private val diccionarioRepository: DiccionarioRepository
) : ViewModel() {

    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var leccionesCompletadas by mutableStateOf(0)
        private set
    var logros by mutableStateOf<List<LogroConEstado>>(emptyList())
        private set
    var favoritos by mutableStateOf<List<PalabraConTraduccion>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val nivelActual: Int get() = (usuario?.xp ?: 0) / XP_POR_NIVEL + 1
    val xpEnNivelActual: Int get() = (usuario?.xp ?: 0) % XP_POR_NIVEL
    val logrosDesbloqueados: Int get() = logros.count { it.desbloqueado }

    fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId()
                    ?: throw IllegalStateException(t(R.string.perfil_sesion_no_iniciada))
                usuario = obtenerUsuarioUseCase(userId)
                coroutineScope {
                    val progresoDeferred = async { obtenerProgresoUseCase(userId) }
                    val logrosDeferred = async { obtenerLogrosConEstadoUseCase(userId) }
                    val favoritosDeferred = async { obtenerFavoritosUseCase(userId).take(3) }

                    leccionesCompletadas = progresoDeferred.await().count { it.estado == "completada" }
                    logros = logrosDeferred.await()
                    favoritos = favoritosDeferred.await().map { fav ->
                        async {
                            val traduccion = diccionarioRepository.obtenerTraducciones(fav.palabra.id)
                                .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL_ID }
                                ?.palabraDestino
                                ?.texto
                            PalabraConTraduccion(texto = fav.palabra.texto, traduccion = traduccion)
                        }
                    }.awaitAll()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.perfil_error_al_cargar_el_perfil)
            } finally {
                isLoading = false
            }
        }
    }
}
