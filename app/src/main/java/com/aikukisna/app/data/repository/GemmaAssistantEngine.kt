package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.ia.MotorGemmaLocal
import com.aikukisna.app.data.local.ia.ModeloTukiLocal
import com.aikukisna.app.domain.assistant.AssistantRequest
import com.aikukisna.app.domain.assistant.AssistantResult
import com.aikukisna.app.domain.assistant.OnDeviceAssistantEngine
import com.aikukisna.app.domain.model.RolChat
import javax.inject.Inject

/** Tuki conversando con Gemma 3 1B dentro del teléfono, sin conexión. */
class GemmaAssistantEngine @Inject constructor(
    private val motor: MotorGemmaLocal,
    private val modelo: ModeloTukiLocal
) : OnDeviceAssistantEngine {

    override fun available(): Boolean = motor.disponible()

    override fun conversational(): Boolean = !modelo.ajustado || modelo.tutor

    override suspend fun answer(request: AssistantRequest): AssistantResult {
        val respuesta = motor.generar(construirPrompt(request))
            .removeSuffix("<end_of_turn>")
            .trim()
        check(respuesta.isNotBlank()) { "Tuki no generó respuesta" }
        return AssistantResult(respuesta, conclusive = true)
    }

    // Gemma no tiene rol de sistema: las instrucciones van al inicio del primer turno del usuario.
    private fun construirPrompt(request: AssistantRequest): String {
        val historial = request.history.dropLast(1).takeLast(MAX_TURNOS_HISTORIAL)
        val instrucciones = request.verifiedContext.take(MAX_CARACTERES_CONTEXTO)
        return buildString {
            append("<start_of_turn>user\n").append(instrucciones).append("\n\n")
            if (historial.isEmpty()) {
                append(request.message.take(MAX_CARACTERES_MENSAJE))
            } else {
                append("(Comienza la conversación)<end_of_turn>\n")
                historial.forEach { mensaje ->
                    val rol = if (mensaje.rol == RolChat.USUARIO) "user" else "model"
                    append("<start_of_turn>").append(rol).append('\n')
                        .append(mensaje.texto.take(MAX_CARACTERES_MENSAJE))
                        .append("<end_of_turn>\n")
                }
                append("<start_of_turn>user\n").append(request.message.take(MAX_CARACTERES_MENSAJE))
            }
            append("<end_of_turn>\n<start_of_turn>model\n")
        }
    }

    private companion object {
        // Límites pensados para que prompt + respuesta quepan en MotorGemmaLocal.MAX_TOKENS.
        // Solo el último intercambio: un historial largo hace que el modelo pequeño copie
        // y amplifique sus propios errores de turnos anteriores.
        const val MAX_TURNOS_HISTORIAL = 4
        const val MAX_CARACTERES_MENSAJE = 220
        const val MAX_CARACTERES_CONTEXTO = 2000
    }
}
