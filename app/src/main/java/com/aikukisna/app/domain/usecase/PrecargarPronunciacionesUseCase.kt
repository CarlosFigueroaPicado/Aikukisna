package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

data class ProgresoPronunciaciones(
    val completadas: Int,
    val total: Int,
    val omitidas: Int
)

/** Precarga pronunciaciones sin repetir archivos ya descargados. */
class PrecargarPronunciacionesUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val obtenerAudio: ObtenerAudioPronunciacionUseCase,
    private val conectividad: NetworkAvailability
) {
    companion object {
        const val LIMITE_POR_DESCARGA = 100
    }

    suspend operator fun invoke(
        idiomaMetaId: Int?,
        onProgress: (ProgresoPronunciaciones) -> Unit
    ) {
        // ElevenLabs solo puede generar audio con internet; no bloqueamos el
        // acceso offline intentando solicitudes que terminarian por timeout.
        if (!conectividad.hayConexion()) {
            onProgress(ProgresoPronunciaciones(0, 0, 0))
            return
        }

        val lecciones = leccionRepository.obtenerLecciones()
            .filter { idiomaMetaId == null || it.idiomaMeta.id == idiomaMetaId }

        val solicitudes = linkedSetOf<SolicitudPronunciacion>()
        lecciones.forEach { leccion ->
            runCatching { leccionRepository.obtenerContenidoLeccion(leccion.id) }
                .getOrNull()
                ?.let { contenido ->
                    when (contenido) {
                        is ContenidoLeccion.Vocabulario -> contenido.palabras.mapTo(solicitudes) {
                            SolicitudPronunciacion(it.texto, it.idioma)
                        }
                        is ContenidoLeccion.Frases -> contenido.oraciones.mapNotNullTo(solicitudes) { oracion ->
                            Idioma.DISPONIBLES.firstOrNull { it.id == oracion.idiomaOrigenId }
                                ?.let { SolicitudPronunciacion(oracion.textoOrigen, it) }
                        }
                    }
                }
        }

        val seleccionadas = solicitudes.filter { it.texto.isNotBlank() }.take(LIMITE_POR_DESCARGA)
        var completadas = 0
        var omitidas = (solicitudes.size - seleccionadas.size).coerceAtLeast(0)
        onProgress(ProgresoPronunciaciones(completadas, seleccionadas.size, omitidas))

        seleccionadas.forEach { solicitud ->
            try {
                val resultado = obtenerAudio(solicitud)
                if (resultado.audio != null) completadas++ else omitidas++
            } catch (_: Exception) {
                omitidas++
            }
            onProgress(ProgresoPronunciaciones(completadas, seleccionadas.size, omitidas))
        }
    }
}
