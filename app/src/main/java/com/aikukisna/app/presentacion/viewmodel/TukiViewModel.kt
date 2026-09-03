package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.ConversarConTukiUseCase
import com.aikukisna.app.domain.usecase.ObtenerContextoTukiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TukiViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val obtenerContextoTukiUseCase: ObtenerContextoTukiUseCase,
    private val conversarConTukiUseCase: ConversarConTukiUseCase
) : ViewModel() {

    var mensajes by mutableStateOf<List<MensajeChat>>(emptyList())
        private set
    var textoEntrada by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var contexto: String? = null
    private var contextoCargado = false

    init {
        viewModelScope.launch {
            val userId = authRepository.usuarioActualId()
            val idiomaNombre = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta?.nombre }
                ?: "tu idioma"
            mensajes = listOf(
                MensajeChat(
                    rol = RolChat.TUKI,
                    texto = "¡Diaki! Soy Tuki. Puedo enseñarte vocabulario, pronunciación y frases del día a día en $idiomaNombre, o cualquier otra cosa que quieras."
                )
            )
        }
    }

    fun onTextoEntradaChange(valor: String) {
        textoEntrada = valor
    }

    fun enviarMensaje() {
        val texto = textoEntrada.trim()
        if (texto.isBlank() || isLoading) return

        textoEntrada = ""
        errorMessage = null
        mensajes = mensajes + MensajeChat(rol = RolChat.USUARIO, texto = texto)

        viewModelScope.launch {
            isLoading = true
            try {
                if (!contextoCargado) {
                    val userId = authRepository.usuarioActualId()
                    val idiomaNombre = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta?.nombre }
                    if (userId != null && idiomaNombre != null) {
                        contexto = obtenerContextoTukiUseCase(userId, idiomaNombre)
                    }
                    contextoCargado = true
                }
                val respuesta = conversarConTukiUseCase(mensajes, contexto)
                mensajes = mensajes + MensajeChat(rol = RolChat.TUKI, texto = respuesta)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo enviar el mensaje"
            } finally {
                isLoading = false
            }
        }
    }
}