package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.IaRepository
import com.aikukisna.app.domain.repository.MemoriaTukiStore
import com.aikukisna.app.domain.repository.NetworkAvailability
import java.util.UUID
import javax.inject.Inject

class ConversarConTukiUseCase @Inject constructor(
    private val responderOffline: TukiOfflineResponder,
    private val iaRepository: IaRepository,
    private val networkAvailability: NetworkAvailability,
    private val memoriaTukiStore: MemoriaTukiStore
) {
    constructor(diccionarioRepository: DiccionarioRepository) : this(
        TukiOfflineResponder(diccionarioRepository),
        IaNoDisponible,
        NetworkAvailability { false },
        MemoriaNoDisponible
    )

    suspend operator fun invoke(
        historial: List<MensajeChat>,
        idiomaMetaId: Int,
        usuarioId: UUID? = null
    ): String {
        require(historial.isNotEmpty()) { "El historial no puede estar vacío" }
        val mensaje = historial.last().texto.trim()
        val respuestaLocal = responderOffline.responder(mensaje, idiomaMetaId, historial)
        if (respuestaLocal.concluyente) {
            guardarMemoria(usuarioId, mensaje, respuestaLocal.texto)
            return respuestaLocal.texto
        }

        val datosDiccionario = responderOffline.construirContexto(mensaje, idiomaMetaId)
        val recuerdos = usuarioId?.let { memoriaTukiStore.recordar(it, mensaje) }.orEmpty()
        if (!networkAvailability.hayConexion()) {
            val respuestaOffline = if (datosDiccionario.isNotEmpty()) {
                "Encontré estas equivalencias verificadas en el diccionario local:\n" +
                    datosDiccionario.joinToString("\n") { "• $it" }
            } else {
                respuestaLocal.texto
            }
            guardarMemoria(usuarioId, mensaje, respuestaOffline)
            return respuestaOffline
        }
        if (datosDiccionario.isEmpty() && recuerdos.isEmpty()) {
            guardarMemoria(usuarioId, mensaje, respuestaLocal.texto)
            return respuestaLocal.texto
        }

        val contexto = buildString {
            appendLine("Responde únicamente con los datos verificados incluidos a continuación.")
            appendLine("Si los datos no bastan, indícalo sin completar información por cuenta propia.")
            if (datosDiccionario.isNotEmpty()) {
                appendLine("Diccionario verificado:")
                datosDiccionario.forEach { appendLine("- $it") }
            }
            if (recuerdos.isNotEmpty()) {
                appendLine("Conversaciones previas del mismo usuario:")
                recuerdos.forEach { appendLine("- $it") }
            }
        }.trim()
        val respuesta = runCatching { iaRepository.conversar(historial, contexto) }
            .getOrElse { respuestaLocal.texto }
            .trim()
            .ifEmpty { respuestaLocal.texto }
        guardarMemoria(usuarioId, mensaje, respuesta)
        return respuesta
    }

    private suspend fun guardarMemoria(usuarioId: UUID?, mensaje: String, respuesta: String) {
        if (usuarioId != null) memoriaTukiStore.aprender(usuarioId, mensaje, respuesta)
    }

    private object IaNoDisponible : IaRepository {
        override suspend fun preguntar(prompt: String) = error("Servicio no disponible")
        override suspend fun conversar(historial: List<MensajeChat>, contexto: String?) = error("Servicio no disponible")
        override suspend fun preguntarConImagen(prompt: String, imagenBase64: String, mimeType: String) = error("Servicio no disponible")
        override suspend fun preguntarConAudio(prompt: String, audioBase64: String, audioMimeType: String) = error("Servicio no disponible")
    }

    private object MemoriaNoDisponible : MemoriaTukiStore {
        override suspend fun recordar(usuarioId: UUID, consulta: String, limite: Int) = emptyList<String>()
        override suspend fun obtenerContexto(usuarioId: UUID) = null
        override suspend fun obtenerMensajesRecientes(usuarioId: UUID, idiomaId: Int?, limiteIntercambios: Int) = emptyList<MensajeChat>()
        override suspend fun aprender(usuarioId: UUID, consulta: String, respuesta: String, contexto: com.aikukisna.app.domain.repository.ContextoConversacionTuki) = Unit
    }
}
