package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject

// Ids fijos según la tabla `idioma` — mismo criterio que el resto del
// proyecto (ver GenerarQuizLeccionUseCase, DictionaryViewModel).
private const val IDIOMA_MISKITO = 1
private const val IDIOMA_ESPANOL = 2
private const val IDIOMA_KRIOL = 3
private const val IDIOMA_INGLES = 4

private val NOMBRE_IDIOMA = mapOf(
    IDIOMA_MISKITO to "Miskito",
    IDIOMA_ESPANOL to "Español",
    IDIOMA_KRIOL to "Inglés Kriol (criollo de la costa caribeña de Nicaragua)",
    IDIOMA_INGLES to "Inglés estándar"
)

/**
 * Traduce texto entre cualquiera de los 4 idiomas de la app (Miskito,
 * Español, Kriol, Inglés estándar). Si es una sola palabra, busca
 * primero en el diccionario ya verificado — es más preciso y no gasta
 * cuota de la API. Si no la encuentra ahí, o es una frase completa, cae a
 * Gemini como respaldo.
 */
class TraducirTextoUseCase @Inject constructor(
    private val diccionarioRepository: DiccionarioRepository,
    private val iaRepository: IaRepository
) {
    suspend operator fun invoke(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): ResultadoTraduccion {
        require(texto.isNotBlank()) { "El texto a traducir no puede estar vacío" }

        val textoLimpio = texto.trim()
        val esUnaSolaPalabra = !textoLimpio.contains(" ")

        if (esUnaSolaPalabra) {
            val delDiccionario = buscarEnDiccionario(textoLimpio, idiomaOrigenId, idiomaDestinoId)
            if (delDiccionario != null) {
                return ResultadoTraduccion(texto = delDiccionario, fuente = FuenteTraduccion.DICCIONARIO)
            }
        }

        return ResultadoTraduccion(
            texto = traducirConIa(textoLimpio, idiomaOrigenId, idiomaDestinoId),
            fuente = FuenteTraduccion.IA
        )
    }

    private suspend fun buscarEnDiccionario(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): String? {
        val candidatos = diccionarioRepository.buscarPalabras(
            query = texto,
            idiomaId = idiomaOrigenId,
            limite = 5,
            offset = 0
        )
        val coincidenciaExacta = candidatos.firstOrNull { it.texto.equals(texto, ignoreCase = true) }
            ?: return null

        return diccionarioRepository.obtenerTraducciones(coincidenciaExacta.id)
            .firstOrNull { it.palabraDestino.idioma.id == idiomaDestinoId }
            ?.palabraDestino
            ?.texto
    }

    private suspend fun traducirConIa(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): String {
        val nombreOrigen = NOMBRE_IDIOMA[idiomaOrigenId] ?: "el idioma de origen"
        val nombreDestino = NOMBRE_IDIOMA[idiomaDestinoId] ?: "el idioma de destino"
        val prompt = "Traduce este texto de $nombreOrigen a $nombreDestino. " +
                "Respondé únicamente con la traducción, sin explicaciones ni comillas: \"$texto\""
        return iaRepository.preguntar(prompt).trim()
    }
}