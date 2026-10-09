package com.aikukisna.app.domain.repository

import java.util.UUID
import com.aikukisna.app.domain.model.MensajeChat

interface MemoriaTukiStore {
    suspend fun recordar(usuarioId: UUID, consulta: String, limite: Int = 8): List<String>
    suspend fun obtenerContexto(usuarioId: UUID): ContextoConversacionTuki?
    /** Con [idiomaId] solo devuelve la conversación de ese idioma: cada idioma tiene su propio chat. */
    suspend fun obtenerMensajesRecientes(usuarioId: UUID, idiomaId: Int? = null, limiteIntercambios: Int = 10): List<MensajeChat>
    suspend fun aprender(
        usuarioId: UUID,
        consulta: String,
        respuesta: String,
        contexto: ContextoConversacionTuki = ContextoConversacionTuki()
    )
}

data class ContextoConversacionTuki(
    val tema: String? = null,
    val ultimaPalabraOFrase: String? = null,
    val ultimaLeccionId: Int? = null,
    val ultimaIntencion: String? = null,
    val idiomaId: Int? = null
)
