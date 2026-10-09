package com.aikukisna.app.domain.repository

/** Traducción generativa que corre en el teléfono (sin conexión) cuando el modelo está descargado. */
interface TraductorAutomaticoLocal {
    fun disponible(): Boolean

    /** Pares que el modelo domina: español ↔ inglés con el modelo base; los cuatro idiomas con el ajustado. */
    fun soporta(idiomaOrigenId: Int, idiomaDestinoId: Int): Boolean

    /** false si el modelo no aprovecha el glosario: así no se gasta tiempo armándolo. */
    fun usaGlosario(): Boolean = true

    /** [glosario] son equivalencias verificadas del diccionario que el modelo debe respetar. */
    suspend fun traducir(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int, glosario: List<String>): String
}
