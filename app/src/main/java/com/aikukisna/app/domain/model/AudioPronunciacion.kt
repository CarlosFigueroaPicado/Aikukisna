package com.aikukisna.app.domain.model

data class AudioPronunciacion(
    val palabraId: Int,
    val idioma: Idioma,
    val referencia: ReferenciaAudioPronunciacion,
    val verificado: Boolean,
    val origen: OrigenAudioPronunciacion
)

enum class OrigenAudioPronunciacion {
    HUMANO,
    SINTETICO,
    DESCONOCIDO
}

sealed interface ReferenciaAudioPronunciacion {
    val valor: String

    data class Asset(override val valor: String) : ReferenciaAudioPronunciacion
    data class ArchivoInterno(override val valor: String) : ReferenciaAudioPronunciacion
}

/**
 * Forma escrita que identifica un audio grabado: sin tildes, sin signos y sin notas entre paréntesis
 * ("¿Utla?" y "utla (casa)" → "utla"). Debe coincidir con `forma()` de scripts/emparejar_audios_hamilton.py.
 */
fun normalizarFormaAudio(texto: String): String {
    val sinNotas = Regex("""\([^)]*\)""").replace(texto.lowercase(), " ")
    val sinTildes = java.text.Normalizer.normalize(sinNotas, java.text.Normalizer.Form.NFD)
        .replace(Regex("""\p{Mn}+"""), "")
    return Regex("[^a-z ]").replace(sinTildes, " ").split(' ').filter { it.isNotBlank() }.joinToString(" ")
}
