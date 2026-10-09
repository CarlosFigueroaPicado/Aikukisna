package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.domain.repository.ReconocedorTextoLocal
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import com.aikukisna.app.domain.repository.TextoEnImagen
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ReconocerTextoEnImagenUseCase @Inject constructor(
    private val traducirTextoUseCase: TraducirTextoUseCase,
    private val reconocedorTextoLocal: ReconocedorTextoLocal,
    private val traducirOracionUseCase: TraducirOracionUseCase? = null,
    private val conocimiento: RepositorioConocimiento? = null
) {
    /**
     * Con [x] e [y] (toque en la vista previa, 0..1) solo se lee y traduce el texto tocado; [alLeer]
     * avisa qué texto se eligió antes de traducirlo, para marcarlo en pantalla.
     */
    suspend operator fun invoke(
        imagenBase64: String,
        idiomaDestinoId: Int,
        x: Float? = null,
        y: Float? = null,
        alLeer: (TextoEnImagen) -> Unit = {}
    ): ResultadoReconocimiento {
        require(imagenBase64.isNotBlank()) {
            "La imagen no puede estar vacía"
        }

        try {
            val leido = if (x != null && y != null) reconocedorTextoLocal.reconocerEnPunto(imagenBase64, x, y)
            else TextoEnImagen(reconocedorTextoLocal.reconocer(imagenBase64), null)
            val textoReconocido = leido.texto.trim()

            if (textoReconocido.isBlank()) {
                error(
                    if (x != null) "No encontré texto donde tocaste. Toca justo encima de la palabra."
                    else "No se encontró texto en la imagen"
                )
            }
            alLeer(leido.copy(texto = textoReconocido))

            // Sin traducción se muestra igual lo que se leyó ("Aún no tengo una traducción verificada"):
            // un error obligaba a tocar de nuevo sin saber siquiera qué texto se había reconocido.
            val conocidas = palabrasConocidasPorIdioma(textoReconocido)
            // Un texto que ya está en el idioma que aprende el estudiante ("Output Data" con inglés) no se
            // traduce a ese mismo idioma: se le muestra qué significa en español.
            val yaEnIdiomaMeta = conocidas.isNotEmpty() && idiomaDestinoId != ESPANOL &&
                conocidas.getValue(idiomaDestinoId) > 0 &&
                conocidas.getValue(idiomaDestinoId) >= conocidas.filterKeys { it != idiomaDestinoId }.values.max()
            val destino = if (yaEnIdiomaMeta) ESPANOL else idiomaDestinoId
            val traduccion = traducirTextoLocal(
                texto = textoReconocido,
                idiomaDestinoId = destino,
                conocidas = conocidas,
                primero = if (yaEnIdiomaMeta) idiomaDestinoId else null
            )

            return ResultadoReconocimiento(
                objetoDetectado = textoReconocido,
                traduccion = traduccion,
                idiomaTraduccionId = destino.takeIf { it != idiomaDestinoId }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(
                e.message ?: "No se pudo leer y traducir el texto de la imagen",
                e
            )
        }
    }

    private suspend fun traducirTextoLocal(
        texto: String,
        idiomaDestinoId: Int,
        conocidas: Map<Int, Int>,
        primero: Int?
    ): String? {
        val idiomasPosibles = ORDEN_IDIOMAS.filterNot { it == idiomaDestinoId }
            // sortedByDescending es estable: con empate se respeta el orden habitual (español primero).
            .sortedByDescending { conocidas[it] ?: 0 }
            .let { lista -> if (primero != null) listOf(primero) + (lista - primero) else lista }

        idiomasPosibles.forEach { idiomaOrigenId ->
            try {
                val resultado = traducirTextoUseCase(texto, idiomaOrigenId, idiomaDestinoId).texto
                if (!esElMismoTexto(resultado, texto)) return resultado
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Probar el siguiente idioma disponible.
            }
        }

        // Carteles y frases rara vez están completos en el corpus: se intenta la traducción
        // de oraciones sin conexión, indicando que no es una traducción verificada. Solo con los
        // dos idiomas más probables: cada intento puede usar el modelo y tardar varios segundos.
        // Si el diccionario no reconoce ninguna palabra (un nombre de archivo, una marca), el modelo
        // solo inventaría ("muestras_tutoria.json" → "tutor"): mejor decir que no hay traducción.
        if (conocidas.isNotEmpty() && conocidas.values.all { it == 0 }) return null
        traducirOracionUseCase?.let { traducirOracion ->
            idiomasPosibles.take(2).forEach { idiomaOrigenId ->
                try {
                    val resultado = traducirOracion(texto, idiomaOrigenId, idiomaDestinoId)
                    // El modelo a veces devuelve el mismo texto: eso no está en el idioma elegido.
                    if (esElMismoTexto(resultado.texto, texto)) return@forEach
                    val etiqueta = when (resultado.tipo) {
                        TipoTraduccion.VERIFICADA -> return resultado.texto
                        TipoTraduccion.LITERAL -> "traducción literal"
                        TipoTraduccion.AUTOMATICA -> "traducción automática"
                    }
                    return "${resultado.texto} ($etiqueta)"
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Probar el siguiente idioma disponible.
                }
            }
        }

        return null
    }

    /**
     * Palabras de [texto] que conoce el diccionario de cada idioma: dice en qué idioma está. Antes se
     * suponía siempre español y un cartel en inglés pasaba al modelo como si fuera español.
     * Vacío si no hay diccionario con qué comparar.
     */
    private suspend fun palabrasConocidasPorIdioma(texto: String): Map<Int, Int> {
        val repositorio = conocimiento ?: return emptyMap()
        val palabras = texto.split(Regex("[^\\p{L}\\p{N}'’-]+")).filter { it.length > 1 }.take(MAX_PALABRAS_DETECCION)
        if (palabras.isEmpty()) return emptyMap()
        return ORDEN_IDIOMAS.associateWith { idioma ->
            runCatching { repositorio.contarPalabrasConocidas(palabras, idioma) }.getOrDefault(0)
        }
    }

    private fun esElMismoTexto(a: String, b: String): Boolean =
        NormalizadorLinguistico.normalizar(a.substringBefore(" (")) == NormalizadorLinguistico.normalizar(b)

    private companion object {
        const val ESPANOL = 2
        val ORDEN_IDIOMAS = listOf(2, 4, 3, 1)
        const val MAX_PALABRAS_DETECCION = 12
    }
}
