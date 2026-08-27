package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.repository.IaRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

// Valor fijo según el CHECK de la tabla memoria_tuki en producción:
// aprendizaje | conversacion | preferencia | recomendacion.
private const val TIPO_CONVERSACION = "conversacion"

// Conversaciones muy cortas (solo el saludo, por ejemplo) no dejan nada
// real que valga la pena recordar — no tiene sentido gastar una llamada
// a Gemini para resumir "hola" + "¡hola! ¿en qué te ayudo?".
private const val MINIMO_MENSAJES_PARA_RESUMIR = 4

/**
 * Al terminar (o pausar) una conversación con Tuki, genera un resumen
 * corto con Gemini y lo guarda como memoria de largo plazo — la próxima
 * conversación arranca sabiendo esto, vía ObtenerContextoTukiUseCase.
 */
class GenerarYGuardarResumenConversacionUseCase @Inject constructor(
    private val iaRepository: IaRepository,
    private val guardarMemoriaTukiUseCase: GuardarMemoriaTukiUseCase
) {
    suspend operator fun invoke(usuarioId: UUID, historial: List<MensajeChat>) {
        if (historial.size < MINIMO_MENSAJES_PARA_RESUMIR) return

        val transcripcion = historial.joinToString(separator = "\n") { mensaje ->
            val quien = if (mensaje.rol == RolChat.USUARIO) "Estudiante" else "Tuki"
            "$quien: ${mensaje.texto}"
        }

        val prompt = "Resumí esta conversación entre un estudiante y Tuki, un asistente que " +
                "enseña Miskito, en una sola oración corta — qué preguntó o qué aprendió el " +
                "estudiante. Sin comillas, sin prefijos, solo la oración:\n\n$transcripcion"

        val resumen = try {
            iaRepository.preguntar(prompt).trim()
        } catch (e: Exception) {
            return // si falla el resumen, no se pierde la conversación en sí, solo no se guarda memoria de ella
        }

        if (resumen.isBlank()) return

        guardarMemoriaTukiUseCase(
            MemoriaTuki(
                id = 0, // ignorado al insertar, la base lo genera sola
                usuarioId = usuarioId,
                tipo = TIPO_CONVERSACION,
                resumen = resumen,
                fecha = Instant.now()
            )
        )
    }
}