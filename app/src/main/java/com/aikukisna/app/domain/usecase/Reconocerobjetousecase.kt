package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.CorpusCamaraRepository
import com.aikukisna.app.domain.repository.EtiquetasObjetoEspanol
import com.aikukisna.app.domain.repository.IaRepository
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.ReconocedorObjetosLocal
import com.aikukisna.app.domain.repository.ReconocedorConceptosLocal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val IDIOMA_ESPANOL = 2
private const val IDIOMA_INGLES = 4
private const val CONFIANZA_CONCEPTO = 0.45f

class ReconocerObjetoUseCase @Inject constructor(
    private val diccionarioRepository: DiccionarioRepository,
    private val traducirTextoUseCase: TraducirTextoUseCase,
    private val iaRepository: IaRepository,
    private val conectividad: NetworkAvailability,
    private val reconocedorObjetosLocal: ReconocedorObjetosLocal,
    private val corpusCamara: CorpusCamaraRepository = CorpusCamaraVacio,
    private val etiquetasEspanol: EtiquetasObjetoEspanol = EtiquetasObjetoEspanol { null },
    private val reconocedorConceptos: ReconocedorConceptosLocal = ReconocedorConceptosLocal { emptyList() }
) {

    suspend operator fun invoke(
        imagenBase64: String,
        idiomaMetaId: Int
    ): ResultadoReconocimiento {
        require(imagenBase64.isNotBlank()) {
            "La imagen no puede estar vacía"
        }

        if (conectividad.hayConexion()) {
            try {
                // Una red lenta no debe dejar al usuario esperando: el reconocimiento local responde enseguida.
                val objetoEnEspanol = withTimeoutOrNull(TIEMPO_MAXIMO_EN_LINEA_MS) {
                    iaRepository.preguntarConImagen(
                        prompt = PROMPT_OBJETO,
                        imagenBase64 = imagenBase64,
                        mimeType = "image/jpeg"
                    )
                }.orEmpty()
                    .trim()
                    .trim('"', '.', ' ', '\n', '\r')

                if (objetoEnEspanol.isNotBlank()) {
                    // Con conexión se prioriza la identificación visual específica.
                    // La traducción continúa limitada al diccionario documentado.
                    return construirResultadoDesdeEspanol(
                        objetoEnEspanol = objetoEnEspanol,
                        idiomaMetaId = idiomaMetaId
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Si falla el reconocimiento en línea, se conserva el respaldo local.
            }
        }

        return reconocerObjetoLocal(
            imagenBase64 = imagenBase64,
            idiomaMetaId = idiomaMetaId
        )
    }

    private suspend fun construirResultadoDesdeEspanol(
        objetoEnEspanol: String,
        idiomaMetaId: Int
    ): ResultadoReconocimiento {
        val identidadControlada = corpusCamara.buscarPorConceptoEspanol(
            objetoEnEspanol,
            IDIOMA_ESPANOL
        )
        if (identidadControlada != null) {
            val seleccion = corpusCamara.buscarPorConceptoEspanol(
                objetoEnEspanol,
                idiomaMetaId
            )
            val palabraEspanol = diccionarioRepository.obtenerPalabraPorId(
                identidadControlada.palabraEspanolId
            )
            val palabraDestino = seleccion?.let {
                diccionarioRepository.obtenerPalabraPorId(it.palabraDestinoId)
            }
            val nombreEspanol = palabraEspanol?.texto ?: objetoEnEspanol
            return ResultadoReconocimiento(
                objetoDetectado = nombreEspanol,
                // No todos los conceptos revisados tienen selección para los cuatro idiomas.
                traduccion = palabraDestino?.texto ?: traducirDesdeEspanol(nombreEspanol, idiomaMetaId)
            )
        }

        return ResultadoReconocimiento(
            objetoDetectado = objetoEnEspanol,
            traduccion = traducirDesdeEspanol(objetoEnEspanol, idiomaMetaId)
        )
    }

    private suspend fun traducirDesdeEspanol(objetoEnEspanol: String, idiomaMetaId: Int): String? {
        if (idiomaMetaId == IDIOMA_ESPANOL) return objetoEnEspanol
        // Una palabra con varias equivalencias ("gato") mostraba el aviso "Hay varias traducciones…"
        // en lugar de la palabra: en la cámara se muestra la mejor registrada.
        buscarPalabraExacta(objetoEnEspanol, IDIOMA_ESPANOL)?.let { palabra ->
            mejorTraduccion(
                diccionarioRepository.obtenerTraducciones(palabra.id)
                    .filter { it.palabraDestino.idioma.id == idiomaMetaId }
            )?.let { return it }
        }
        return buscarTraduccionEnDiccionario(objetoEnEspanol, IDIOMA_ESPANOL, idiomaMetaId)
    }

    private suspend fun reconocerObjetoLocal(
        imagenBase64: String,
        idiomaMetaId: Int
    ): ResultadoReconocimiento {
        // Primero el vocabulario propio (TinyCLIP): reconoce coco, mango, machete o panga, que el
        // clasificador de ImageNet no conoce. Si no está seguro, se usa ML Kit como respaldo.
        val concepto = runCatching { reconocedorConceptos.reconocer(imagenBase64).firstOrNull() }
            .getOrNull()
            ?.takeIf { it.confianza >= CONFIANZA_CONCEPTO }
        if (concepto != null) {
            return construirResultadoDesdeEspanol(concepto.espanol, idiomaMetaId).let {
                if (it.traduccion == null && idiomaMetaId == IDIOMA_INGLES) it.copy(traduccion = traducirDesdeEspanol(concepto.espanol, IDIOMA_INGLES)) else it
            }
        }

        val etiquetas = reconocedorObjetosLocal.reconocer(imagenBase64)

        if (etiquetas.isEmpty()) {
            error("No se pudo reconocer ningún objeto en la imagen")
        }
        var sinTraduccion: ResultadoReconocimiento? = null

        etiquetas.forEach { etiquetaEnIngles ->
            val identidadControlada = corpusCamara.buscarPorEtiquetaIngles(
                etiquetaEnIngles,
                IDIOMA_ESPANOL
            )
            if (identidadControlada != null) {
                val seleccion = corpusCamara.buscarPorEtiquetaIngles(
                    etiquetaEnIngles,
                    idiomaMetaId
                )
                val palabraEspanol = diccionarioRepository.obtenerPalabraPorId(
                    identidadControlada.palabraEspanolId
                ) ?: return@forEach
                val palabraDestino = seleccion?.let {
                    diccionarioRepository.obtenerPalabraPorId(it.palabraDestinoId)
                }
                // El corpus revisado no cubre los cuatro idiomas: sin selección se usa el diccionario
                // y, en inglés, la propia etiqueta del reconocedor ("laptop").
                return ResultadoReconocimiento(
                    objetoDetectado = palabraEspanol.texto,
                    traduccion = palabraDestino?.texto
                        ?: traducirDesdeEspanol(palabraEspanol.texto, idiomaMetaId)
                        ?: etiquetaEnIngles.lowercase().takeIf { idiomaMetaId == IDIOMA_INGLES }
                )
            }

            // La tabla incluida da el nombre común del objeto; el diccionario inglés confunde
            // etiquetas con varios sentidos ("ear" de maíz → "oreja"), por eso va después.
            val palabraEnEspanol = etiquetasEspanol.espanolPara(etiquetaEnIngles)
                ?: buscarPalabraExacta(
                    texto = etiquetaEnIngles,
                    idiomaId = IDIOMA_INGLES
                )?.let { palabraEnIngles ->
                    diccionarioRepository.obtenerTraducciones(palabraEnIngles.id)
                        .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                        ?.palabraDestino
                        ?.texto
                }
                ?: return@forEach

            val resultado = construirResultadoDesdeEspanol(palabraEnEspanol, idiomaMetaId).let {
                // La etiqueta del reconocedor ya está en inglés.
                if (it.traduccion == null && idiomaMetaId == IDIOMA_INGLES) it.copy(traduccion = etiquetaEnIngles.lowercase())
                else it
            }
            if (resultado.traduccion != null) return resultado
            // Se conserva el primer objeto identificado por si ninguna etiqueta tiene traducción.
            if (sinTraduccion == null) sinTraduccion = resultado
        }

        // Mejor mostrar qué objeto es (en español) que un error: la UI indica que falta traducción.
        return sinTraduccion
            ?: error("El objeto reconocido no tiene una coincidencia documentada en el diccionario local")
    }

    private suspend fun buscarTraduccionEnDiccionario(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): String? = runCatching {
        val resultado = traducirTextoUseCase(texto, idiomaOrigenId, idiomaDestinoId)
        resultado.alternativas.firstOrNull()?.substringBefore(" — ") ?: resultado.texto
    }.getOrNull()

    private suspend fun buscarPalabraExacta(
        texto: String,
        idiomaId: Int
    ) = diccionarioRepository
        .buscarPalabras(
            query = texto,
            idiomaId = idiomaId,
            limite = 10,
            offset = 0
        )
        .firstOrNull {
            normalizarTexto(it.texto) == normalizarTexto(texto)
        }

    private fun normalizarTexto(texto: String): String =
        java.text.Normalizer
            .normalize(
                texto.trim().lowercase(),
                java.text.Normalizer.Form.NFD
            )
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[\\p{P}\\p{S}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private companion object {
        const val TIEMPO_MAXIMO_EN_LINEA_MS = 6_000L
        const val PROMPT_OBJETO =
            "Identifica el objeto principal de la imagen. Responde únicamente con el nombre común genérico del objeto en español, sin marca, modelo, artículos, explicaciones ni puntuación. Por ejemplo, responde computadora portátil y no el modelo del equipo."
    }
}

private object CorpusCamaraVacio : CorpusCamaraRepository {
    override suspend fun buscarPorEtiquetaIngles(
        etiqueta: String,
        idiomaDestinoId: Int
    ) = null

    override suspend fun buscarPorConceptoEspanol(
        concepto: String,
        idiomaDestinoId: Int
    ) = null
}
