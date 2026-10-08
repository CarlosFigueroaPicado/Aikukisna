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
import com.aikukisna.app.domain.usecase.ActualizarPerfilUseCase
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletarPerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val actualizarPerfilUseCase: ActualizarPerfilUseCase
) : ViewModel() {
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var nombre by mutableStateOf("")
    var apellido by mutableStateOf("")
    var nombreUsuario by mutableStateOf("")
    var edad by mutableStateOf("")
    var pais by mutableStateOf("")
    var ciudad by mutableStateOf("")
    var fotoPerfilUri by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var guardado by mutableStateOf(false)
        private set

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                val id = authRepository.usuarioActualId() ?: error(t(R.string.completarperfil_sesion_no_iniciada))
                obtenerUsuarioUseCase(id)
            }.onSuccess { perfil ->
                usuario = perfil
                nombre = perfil?.nombre.orEmpty()
                apellido = perfil?.apellido.orEmpty()
                nombreUsuario = perfil?.nombreUsuario.orEmpty()
                edad = perfil?.edad?.toString().orEmpty()
                pais = perfil?.pais.orEmpty()
                ciudad = perfil?.ciudad.orEmpty()
                fotoPerfilUri = perfil?.fotoPerfilUri
            }.onFailure { errorMessage = it.message ?: t(R.string.completarperfil_no_se_pudo_cargar_tu) }
            isLoading = false
        }
    }

    fun guardar() {
        if (nombre.isBlank() || apellido.isBlank() || nombreUsuario.isBlank()) {
            errorMessage = t(R.string.completarperfil_completa_tu_nombre_apellido_y)
            return
        }
        val edadNumerica = edad.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (edad.isNotBlank() && (edadNumerica == null || edadNumerica !in 5..120)) {
            errorMessage = t(R.string.completarperfil_ingresa_una_edad_valida_entre)
            return
        }
        val actual = usuario ?: run {
            errorMessage = t(R.string.completarperfil_no_se_encontro_tu_perfil)
            return
        }
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            runCatching {
                actualizarPerfilUseCase(
                    actual.copy(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        nombreUsuario = nombreUsuario.trim().lowercase(),
                        edad = edadNumerica,
                        pais = pais.trim().ifEmpty { null },
                        ciudad = ciudad.trim().ifEmpty { null },
                        fotoPerfilUri = fotoPerfilUri
                    )
                )
            }.onSuccess { guardado = true }
                .onFailure { errorMessage = it.message ?: t(R.string.completarperfil_no_se_pudo_guardar_tu) }
            isSaving = false
        }
    }
}
