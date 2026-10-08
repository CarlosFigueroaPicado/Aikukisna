package com.aikukisna.app.data.local.ia

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Ejecuta Gemma en el dispositivo con MediaPipe. Cargar el modelo tarda unos segundos,
 * por eso la instancia se crea una vez y se reutiliza; cada consulta abre su propia sesión.
 */
@Singleton
class MotorGemmaLocal @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelo: ModeloTukiLocal
) {
    private val mutex = Mutex()
    private var inferencia: LlmInference? = null

    fun disponible(): Boolean = modelo.estaDisponible()

    /**
     * Genera en streaming y corta en cuanto la respuesta supera [maxCaracteres] o entra en un
     * bucle (un modelo de 1B tiende a repetir frases o tokens hasta agotar el contexto).
     */
    suspend fun generar(
        prompt: String,
        temperatura: Float = 0.4f,
        topK: Int = 20,
        maxCaracteres: Int = 600,
        unaLinea: Boolean = false
    ): String = withContext(Dispatchers.Default) {
        // El motor nativo no admite consultas simultáneas sobre la misma instancia.
        mutex.withLock {
            val motor = inferencia ?: crearInferencia().also { inferencia = it }
            val opciones = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTemperature(temperatura)
                .setTopK(topK)
                .setTopP(0.9f)
                .build()
            LlmInferenceSession.createFromOptions(motor, opciones).use { sesion ->
                sesion.addQueryChunk(prompt)
                val texto = StringBuffer()
                val futuro = sesion.generateResponseAsync { parcial, _ -> texto.append(parcial) }
                var cancelado = false
                // Se vigila desde este hilo: cancelar dentro del callback nativo no es seguro.
                while (!futuro.isDone) {
                    Thread.sleep(INTERVALO_VIGILANCIA_MS)
                    val actual = texto.toString()
                    // Una traducción ocupa una línea: lo que venga después es el modelo divagando.
                    val lineaTerminada = unaLinea && actual.trimStart().contains('\n')
                    if (!cancelado && (lineaTerminada || RecorteRespuesta.debeCortar(actual, maxCaracteres))) {
                        cancelado = true
                        sesion.cancelGenerateResponseAsync()
                    }
                }
                runCatching { futuro.get() }
                val salida = if (unaLinea) texto.toString().trimStart().substringBefore('\n') else texto.toString()
                RecorteRespuesta.limpiar(salida, maxCaracteres)
            }
        }
    }

    suspend fun liberar() = mutex.withLock {
        inferencia?.close()
        inferencia = null
    }

    private fun crearInferencia(): LlmInference {
        check(modelo.estaDisponible()) { "El modelo de Tuki todavía no está descargado" }
        val opciones = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelo.archivo.absolutePath)
            .setMaxTokens(MAX_TOKENS)
            .setMaxTopK(64)
            .build()
        return LlmInference.createFromOptions(context, opciones)
    }

    companion object {
        /** Presupuesto total (prompt + respuesta) del contexto de Gemma 3 1B en el teléfono. */
        const val MAX_TOKENS = 1536
        private const val INTERVALO_VIGILANCIA_MS = 40L
    }
}

/** Detecta y limpia las degeneraciones típicas de un modelo pequeño. */
internal object RecorteRespuesta {
    private val FIN_ORACION = Regex("(?<=[.!?])\\s+")

    fun debeCortar(texto: String, maxCaracteres: Int): Boolean =
        texto.length > maxCaracteres || tokenRepetido(texto) || oracionRepetida(texto)

    fun limpiar(texto: String, maxCaracteres: Int): String {
        val sinMarcas = texto.replace("<end_of_turn>", "").replace(Regex("[ \\t]{2,}"), " ").trim()
        // Se quitan oraciones repetidas y la cola de un bucle de tokens.
        val oraciones = mutableListOf<String>()
        for (oracion in sinMarcas.split(FIN_ORACION)) {
            val limpia = oracion.trim()
            if (limpia.isEmpty() || tokenRepetido(limpia)) break
            if (oraciones.any { normalizar(it) == normalizar(limpia) }) continue
            oraciones += limpia
        }
        var resultado = oraciones.joinToString(" ")
        if (resultado.length > maxCaracteres) {
            val corte = resultado.take(maxCaracteres)
            val ultimoFin = corte.lastIndexOfAny(charArrayOf('.', '!', '?'))
            resultado = if (ultimoFin > maxCaracteres / 3) corte.substring(0, ultimoFin + 1) else "$corte…"
        }
        return resultado.trim()
    }

    // Ej.: "7 7 7 7 7 7" o "como como como como".
    private fun tokenRepetido(texto: String): Boolean {
        val palabras = texto.split(Regex("\\s+")).filter { it.isNotBlank() }.takeLast(8)
        return palabras.size >= 6 && palabras.distinct().size <= 2
    }

    private fun oracionRepetida(texto: String): Boolean {
        val oraciones = texto.split(FIN_ORACION).map(::normalizar).filter { it.length > 15 }
        return oraciones.size != oraciones.distinct().size
    }

    private fun normalizar(texto: String) = texto.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
}
