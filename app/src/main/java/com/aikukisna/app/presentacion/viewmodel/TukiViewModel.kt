package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.TranscribirAudioUseCase
import com.aikukisna.app.domain.assistant.TukiAssistant
import com.aikukisna.app.domain.assistant.TukiIdentity
import com.aikukisna.app.data.local.SembradorReplicaSupabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TukiViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val tukiAssistant: TukiAssistant,
    private val transcribirAudioUseCase: TranscribirAudioUseCase,
    private val sembradorReplica: SembradorReplicaSupabase,
) : ViewModel() {

    var mensajes by mutableStateOf<List<MensajeChat>>(emptyList())
        private set
    var textoEntrada by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isTranscribing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var idiomaMetaId: Int? = null
    private var usuarioId: UUID? = null

    init {
        viewModelScope.launch {
            try {
                sembradorReplica.sembrarSiExiste()
                check(sembradorReplica.hayContenidoDisponible()) {
                    t(R.string.tuki_el_contenido_local_todavia_no)
                }
                val userId = authRepository.usuarioActualId()
                usuarioId = userId
                val idiomaMeta = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta }
                idiomaMetaId = idiomaMeta?.id
                val idiomaNombre = idiomaMeta?.nombre ?: t(R.string.tuki_el_idioma_seleccionado)
                val previous = tukiAssistant.recentHistory(userId, idiomaMeta?.id)
                mensajes = if (previous.isNotEmpty()) previous else listOf(
                    MensajeChat(
                        rol = RolChat.TUKI,
                        texto = t(R.string.tuki_hoy_podemos_practicar_continuar_una, TukiIdentity.greeting, idiomaNombre)
                    )
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.tuki_no_se_pudo_preparar_el)
            } finally {
                isLoading = false
            }
        }
    }

    fun onTextoEntradaChange(valor: String) {
        textoEntrada = valor
    }

    fun enviarMensaje() {
        val texto = textoEntrada.trim()
        if (texto.isBlank() || isLoading) return
        val idiomaSeleccionadoId = idiomaMetaId
        if (idiomaSeleccionadoId == null) {
            errorMessage = t(R.string.tuki_no_se_encontro_el_idioma)
            return
        }

        textoEntrada = ""
        errorMessage = null
        mensajes = mensajes + MensajeChat(rol = RolChat.USUARIO, texto = texto)

        viewModelScope.launch {
            isLoading = true
            val inicio = System.currentTimeMillis()
            try {
                // El idioma puede cambiarse desde Inicio mientras esta pantalla sigue viva.
                val idiomaActual = usuarioId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta?.id }
                    ?: idiomaSeleccionadoId
                idiomaMetaId = idiomaActual
                val respuesta = tukiAssistant.respond(mensajes, idiomaActual, usuarioId)
                mensajes = mensajes + MensajeChat(rol = RolChat.TUKI, texto = respuesta)
                Log.i("AIK_TUKI", "respuesta en ${System.currentTimeMillis() - inicio} ms")
            } catch (e: Exception) {
                errorMessage = t(R.string.tuki_no_se_pudo_obtener_una)
            } finally {
                isLoading = false
            }
        }
    }

    fun transcribirAudio(audioBase64: String) {
        if (isTranscribing) return
        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null
            try {
                textoEntrada = transcribirAudioUseCase(audioBase64, idiomaCodigo = "es")
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.tuki_no_se_pudo_reconocer_el)
            } finally {
                isTranscribing = false
            }
        }
    }

}
