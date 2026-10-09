package com.aikukisna.app.domain.model

data class SolicitudPronunciacion(
    val texto: String,
    val idioma: Idioma,
    val voiceId: String? = null,
    val palabraId: Int? = null
)

sealed interface ResultadoPronunciacion {
    val audio: ByteArray?

    data class AudioLocal(override val audio: ByteArray) : ResultadoPronunciacion
    data class AudioCacheado(override val audio: ByteArray) : ResultadoPronunciacion
    data class AudioRemoto(override val audio: ByteArray) : ResultadoPronunciacion
    data object NoDisponible : ResultadoPronunciacion {
        override val audio: ByteArray? = null
    }
}
