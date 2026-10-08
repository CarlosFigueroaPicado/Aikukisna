package com.aikukisna.app.domain.assistant

import com.aikukisna.app.domain.model.Idioma
import java.text.Normalizer

/** Lo que Tuki sabe de los cuatro idiomas de Aikukisna y cómo reconoce cuándo se mencionan. */
internal object IdiomasTuki {
    const val MISKITO = 1
    const val ESPANOL = 2
    const val KRIOL = 3
    const val INGLES = 4

    private val KRIOL_REGEX = Regex("\\b(ingles )?(kriol|criollo|creole)\\b")
    private val INGLES_REGEX = Regex("\\b(ingles|english)( estandar)?\\b")
    private val MISKITO_REGEX = Regex("\\b(miskito|miskitu|misquito|miskitos)\\b")
    private val ESPANOL_REGEX = Regex("\\b(espanol|castellano)\\b")

    fun nombre(idiomaId: Int): String =
        Idioma.DISPONIBLES.firstOrNull { it.id == idiomaId }?.nombre ?: "el idioma seleccionado"

    /** Idiomas nombrados en el mensaje. "Inglés Kriol" cuenta solo como Kriol. */
    fun mencionados(texto: String): Set<Int> {
        var valor = normalizar(texto)
        val encontrados = mutableSetOf<Int>()
        if (KRIOL_REGEX.containsMatchIn(valor)) {
            encontrados += KRIOL
            valor = KRIOL_REGEX.replace(valor, " ")
        }
        if (INGLES_REGEX.containsMatchIn(valor)) encontrados += INGLES
        if (MISKITO_REGEX.containsMatchIn(valor)) encontrados += MISKITO
        if (ESPANOL_REGEX.containsMatchIn(valor)) encontrados += ESPANOL
        return encontrados
    }

    /** Idiomas pedidos que el estudiante no está aprendiendo; el español es la lengua de la app y no cuenta. */
    fun otrosIdiomasPedidos(texto: String, idiomaMetaId: Int): Set<Int> =
        mencionados(texto) - idiomaMetaId - ESPANOL

    fun redirigirAOtroIdioma(idiomaMetaId: Int, otros: Set<Int>): String {
        val meta = nombre(idiomaMetaId)
        val pedidos = otros.map(::nombre).joinToString(" y ")
        return "Por el momento estás aprendiendo $meta, así que te acompaño con contenido de $meta. " +
            "Si quieres practicar $pedidos, cámbialo desde la pantalla principal (Inicio): toca el idioma que aparece arriba y elige otro. " +
            "¿Seguimos con $meta mientras tanto?"
    }

    fun idiomasDisponibles(idiomaMetaId: Int?): String = buildString {
        append("En Aikukisna puedes aprender Miskito, Inglés Kriol, Inglés Estándar y Español de la Costa Caribe. ")
        if (idiomaMetaId != null) append("Por el momento estás aprendiendo ${nombre(idiomaMetaId)}. ")
        append("Para cambiarlo, ve a la pantalla principal (Inicio) y toca el idioma que aparece arriba.")
    }

    /** Contexto que reciben los modelos generativos para no confundir los idiomas. */
    fun contextoModelo(idiomaMetaId: Int): String {
        val meta = nombre(idiomaMetaId)
        return "El estudiante está aprendiendo $meta. Los cuatro idiomas de la app son Miskito, Inglés Kriol, Inglés Estándar y Español. " +
            "Habla de $meta; si pide otro idioma, recuérdale que está aprendiendo $meta y que puede cambiarlo en la pantalla principal (Inicio)."
    }

    private fun normalizar(texto: String): String = Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
}
