package com.aikukisna.app.domain.assistant

import com.aikukisna.app.domain.model.MensajeChat

interface AssistantEngine {
    suspend fun answer(request: AssistantRequest): AssistantResult
}

data class AssistantRequest(
    val history: List<MensajeChat>,
    val message: String,
    val languageId: Int,
    val verifiedContext: String = ""
)

/**
 * [conversational] marca respuestas de cortesía hechas con plantillas (saludos, gracias...):
 * son válidas, pero un motor generativo puede darlas con más naturalidad.
 */
data class AssistantResult(
    val text: String,
    val conclusive: Boolean,
    val conversational: Boolean = false
)

interface LocalAssistantEngine : AssistantEngine
interface RemoteAssistantEngine : AssistantEngine

/** Modelo generativo que corre en el teléfono; funciona sin conexión cuando está descargado. */
interface OnDeviceAssistantEngine : AssistantEngine {
    fun available(): Boolean

    /**
     * false cuando el modelo descargado es el ajustado solo para traducir: sirve al traductor,
     * pero al conversar repite la pregunta o inventa, así que Tuki no lo usa para charlar.
     */
    fun conversational(): Boolean = true
}
