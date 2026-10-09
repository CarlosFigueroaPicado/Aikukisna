package com.aikukisna.app.domain.repository

fun interface ReconocedorObjetosLocal {
    suspend fun reconocer(imagenBase64: String): List<String>
}

data class RegionObjeto(
    val izquierda: Float,
    val arriba: Float,
    val derecha: Float,
    val abajo: Float
) {
    val area: Float get() = (derecha - izquierda) * (abajo - arriba)
    fun contiene(x: Float, y: Float): Boolean = x in izquierda..derecha && y in arriba..abajo
}

fun interface DetectorObjetosLocal {
    suspend fun detectar(imagenBase64: String): List<RegionObjeto>
}

fun interface ReconocedorTextoLocal {
    suspend fun reconocer(imagenBase64: String): String

    /** Solo el texto que tocó el estudiante en ([x], [y], coordenadas 0..1) y dónde está. */
    suspend fun reconocerEnPunto(imagenBase64: String, x: Float, y: Float): TextoEnImagen =
        TextoEnImagen(reconocer(imagenBase64), null)
}

/** Texto leído en la imagen; [region] permite marcarlo en la vista previa. */
data class TextoEnImagen(val texto: String, val region: RegionObjeto?)

/** Una línea leída por el OCR; [bloque] agrupa las líneas de un mismo párrafo o cartel. */
data class LineaTexto(val texto: String, val region: RegionObjeto, val bloque: Int)

/**
 * Elige el texto tocado: la línea bajo el dedo (o la más cercana, si el toque cae un poco fuera).
 * Un cartel corto de hasta 3 líneas ("SALIDA DE / EMERGENCIA") se toma completo. Si el toque está
 * lejos de todo texto devuelve null, para no traducir un texto que el estudiante no eligió.
 */
fun elegirTextoTocado(lineas: List<LineaTexto>, x: Float, y: Float, distanciaMaxima: Float = 0.12f): TextoEnImagen? {
    fun distancia(r: RegionObjeto): Float {
        val dx = maxOf(r.izquierda - x, 0f, x - r.derecha)
        val dy = maxOf(r.arriba - y, 0f, y - r.abajo)
        return kotlin.math.hypot(dx, dy)
    }
    val tocada = lineas.filter { it.texto.isNotBlank() }.minByOrNull { distancia(it.region) } ?: return null
    if (distancia(tocada.region) > distanciaMaxima) return null
    val delBloque = lineas.filter { it.bloque == tocada.bloque && it.texto.isNotBlank() }
    if (delBloque.size !in 2..3) return TextoEnImagen(tocada.texto.trim(), tocada.region)
    val region = RegionObjeto(
        izquierda = delBloque.minOf { it.region.izquierda },
        arriba = delBloque.minOf { it.region.arriba },
        derecha = delBloque.maxOf { it.region.derecha },
        abajo = delBloque.maxOf { it.region.abajo }
    )
    return TextoEnImagen(delBloque.joinToString(" ") { it.texto.trim() }, region)
}

/** Nombre común en español de una etiqueta del clasificador de imágenes local (en inglés). */
fun interface EtiquetasObjetoEspanol {
    fun espanolPara(etiquetaIngles: String): String?
}

/** Recorta [region] (coordenadas normalizadas 0..1) de una imagen JPEG en base64. */
fun interface RecortadorImagen {
    fun recortar(imagenBase64: String, region: RegionObjeto): String
}

/** Concepto reconocido por su nombre en español, que es la clave para buscarlo en el diccionario. */
data class ConceptoReconocido(val espanol: String, val confianza: Float)

/** Reconoce objetos de un vocabulario propio (coco, machete, panga…) sin conexión. */
fun interface ReconocedorConceptosLocal {
    suspend fun reconocer(imagenBase64: String): List<ConceptoReconocido>
}
