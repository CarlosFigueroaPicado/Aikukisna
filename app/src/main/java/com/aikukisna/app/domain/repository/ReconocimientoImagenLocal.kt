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
