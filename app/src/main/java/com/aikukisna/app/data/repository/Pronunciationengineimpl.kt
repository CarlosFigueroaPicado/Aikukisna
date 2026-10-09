package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.model.ResultadoPronunciacion
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.domain.model.OrigenAudioPronunciacion
import com.aikukisna.app.domain.repository.AudioPronunciacionRepository
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.PronunciationEngine
import com.aikukisna.app.domain.repository.PronunciationStorage
import com.aikukisna.app.domain.repository.SintesisVozLocal
import com.aikukisna.app.domain.repository.VozRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class PronunciationEngineImpl @Inject constructor(
    private val storage: PronunciationStorage,
    private val audioRepository: AudioPronunciacionRepository,
    private val vozRepository: VozRepository,
    private val networkAvailability: NetworkAvailability,
    private val sintesisLocal: SintesisVozLocal = SintesisVozLocal { _, _ -> null }
) : PronunciationEngine {

    override suspend fun resolver(solicitud: SolicitudPronunciacion): ResultadoPronunciacion {
        require(solicitud.texto.isNotBlank()) { "El texto no puede estar vacío" }

        val esMiskito = solicitud.idioma.codigo.equals(CODIGO_MISKITO, ignoreCase = true)

        solicitud.palabraId?.let { palabraId ->
            val audios = audioRepository.buscarVerificados(palabraId, solicitud.idioma)
                .asSequence()
                .filter { audio ->
                        audio.palabraId == palabraId &&
                        audio.idioma.codigo.equals(solicitud.idioma.codigo, ignoreCase = true) &&
                        audio.verificado &&
                        (audio.origen == OrigenAudioPronunciacion.HUMANO ||
                            (!esMiskito && audio.origen == OrigenAudioPronunciacion.SINTETICO))
                }
                .sortedBy { audio ->
                    if (audio.origen == OrigenAudioPronunciacion.HUMANO) 0 else 1
                }

            audios.forEach { audio ->
                audioRepository.leerAudio(audio)?.let {
                    return ResultadoPronunciacion.AudioLocal(it)
                }
            }
        }

        // Sin id (traductor, Tuki): se busca la grabación humana por la forma escrita.
        audioRepository.buscarHumanosPorTexto(solicitud.texto, solicitud.idioma)
            .filter { it.verificado && it.origen == OrigenAudioPronunciacion.HUMANO }
            .forEach { audio ->
                audioRepository.leerAudio(audio)?.let { return ResultadoPronunciacion.AudioLocal(it) }
            }

        if (esMiskito) {
            return ResultadoPronunciacion.NoDisponible
        }

        storage.leer(solicitud)?.let { return it }
        if (networkAvailability.hayConexion()) {
            try {
                val audio = vozRepository.sintetizarVoz(solicitud.texto, solicitud.voiceId)
                storage.guardar(solicitud, audio)
                return ResultadoPronunciacion.AudioRemoto(audio)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Se intenta la voz del sistema.
            }
        }
        return sintetizarSinConexion(solicitud)
    }

    // Solo español e inglés: una voz de otro idioma pronunciaría mal el Kriol.
    private suspend fun sintetizarSinConexion(solicitud: SolicitudPronunciacion): ResultadoPronunciacion {
        val codigo = solicitud.idioma.codigo.lowercase()
        if (codigo !in CODIGOS_VOZ_SISTEMA) return ResultadoPronunciacion.NoDisponible
        val audio = try {
            sintesisLocal.sintetizar(solicitud.texto, codigo)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } ?: return ResultadoPronunciacion.NoDisponible
        storage.guardar(solicitud, audio)
        return ResultadoPronunciacion.AudioLocal(audio)
    }

    private companion object {
        const val CODIGO_MISKITO = "mi"
        val CODIGOS_VOZ_SISTEMA = setOf("es", "en")
    }
}
