package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.PronunciationCacheKey
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.AudioPronunciacion
import com.aikukisna.app.domain.model.OrigenAudioPronunciacion
import com.aikukisna.app.domain.model.ReferenciaAudioPronunciacion
import com.aikukisna.app.domain.model.ResultadoPronunciacion
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.domain.model.normalizarFormaAudio
import com.aikukisna.app.domain.repository.AudioPronunciacionRepository
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.PronunciationStorage
import com.aikukisna.app.domain.repository.VozRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PronunciationEngineImplTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")

    @Test
    fun mismoTextoEnIdiomasDiferentesNoComparteClaveDeCache() {
        val miskitoKey = PronunciationCacheKey.crear("hola", miskito.codigo, null)
        val espanolKey = PronunciationCacheKey.crear("hola", espanol.codigo, null)

        assertNotEquals(miskitoKey, espanolKey)
    }

    @Test
    fun miskitoSinAudioValidoDevuelveNoDisponible() = runBlocking {
        val remoto = RemotoPrueba()
        val engine = engine(storage = StoragePrueba(), remoto = remoto, conectado = true)

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun miskitoNuncaSeleccionaProveedorRemoto() = runBlocking {
        val remoto = RemotoPrueba()
        val engine = engine(storage = StoragePrueba(), remoto = remoto, conectado = true)

        engine.resolver(SolicitudPronunciacion("Naksa", miskito))

        assertEquals(0, remoto.llamadas)
    }

    @Test
    fun miskitoConAudioHumanoVerificadoEstaDisponible() = runBlocking {
        val audio = audio(palabraId = 10, idioma = miskito, origen = OrigenAudioPronunciacion.HUMANO)
        val engine = engine(audioRepository = AudioRepositoryPrueba(listOf(audio)))

        val resultado = engine.resolver(
            SolicitudPronunciacion("Naksa", miskito, palabraId = 10)
        )

        assertTrue(resultado is ResultadoPronunciacion.AudioLocal)
        assertArrayEquals(byteArrayOf(1), resultado.audio)
    }

    @Test
    fun miskitoConAudioNoVerificadoNoEstaDisponible() = runBlocking {
        val audio = audio(10, miskito, OrigenAudioPronunciacion.HUMANO, verificado = false)
        val engine = engine(audioRepository = AudioRepositoryPrueba(listOf(audio)))

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun miskitoConAudioSinteticoNoEstaDisponible() = runBlocking {
        val audio = audio(10, miskito, OrigenAudioPronunciacion.SINTETICO)
        val engine = engine(audioRepository = AudioRepositoryPrueba(listOf(audio)))

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun audioDeOtroIdiomaNoPuedeUtilizarse() = runBlocking {
        val audio = audio(10, espanol, OrigenAudioPronunciacion.HUMANO)
        val engine = engine(audioRepository = AudioRepositoryPrueba(listOf(audio)))

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun palabraNoPuedeUtilizarAudioDeOtraPalabra() = runBlocking {
        val audio = audio(11, miskito, OrigenAudioPronunciacion.HUMANO)
        val engine = engine(audioRepository = AudioRepositoryPrueba(listOf(audio)))

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun audioHumanoVerificadoTienePrioridadSobreSintetico() = runBlocking {
        val sintetico = audio(10, espanol, OrigenAudioPronunciacion.SINTETICO, "sintetico")
        val humano = audio(10, espanol, OrigenAudioPronunciacion.HUMANO, "humano")
        val repositorio = AudioRepositoryPrueba(
            audios = listOf(sintetico, humano),
            contenidos = mapOf("sintetico" to byteArrayOf(2), "humano" to byteArrayOf(1))
        )
        val engine = engine(audioRepository = repositorio)

        val resultado = engine.resolver(SolicitudPronunciacion("Hola", espanol, palabraId = 10))

        assertArrayEquals(byteArrayOf(1), resultado.audio)
    }

    @Test
    fun traductorSinIdUsaGrabacionHumanaPorTexto() = runBlocking {
        val humano = audio(10, miskito, OrigenAudioPronunciacion.HUMANO)
        val engine = engine(audioRepository = AudioRepositoryPrueba(porTexto = mapOf("utla" to listOf(humano))))

        val resultado = engine.resolver(SolicitudPronunciacion("¿Utla?", miskito))

        assertArrayEquals(byteArrayOf(1), resultado.audio)
    }

    @Test
    fun formaDeAudioIgnoraSignosTildesYNotas() {
        assertEquals("utla", normalizarFormaAudio("¿Utla?"))
        assertEquals("dia dukiara", normalizarFormaAudio("¿dîa dukiara? (para qué)"))
    }

    @Test
    fun referenciaInexistenteNoEstaDisponible() = runBlocking {
        val audio = audio(10, miskito, OrigenAudioPronunciacion.HUMANO, "inexistente")
        val engine = engine(
            audioRepository = AudioRepositoryPrueba(listOf(audio), contenidos = emptyMap())
        )

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun wavHeredadoSinAsociacionNoSeConsideraVerificado() = runBlocking {
        val heredado = ResultadoPronunciacion.AudioLocal(byteArrayOf(8))
        val engine = engine(storage = StoragePrueba(heredado))

        val resultado = engine.resolver(SolicitudPronunciacion("Naksa", miskito, palabraId = 10))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
    }

    @Test
    fun foneticaSinAudioNoProduceAudioNiSeleccionaProveedorRemoto() = runBlocking {
        val palabra = Palabra(
            id = 1,
            idioma = miskito,
            texto = "Naksa",
            categoria = null,
            fuente = FuenteDocumento(1, "Fuente", null, null, null),
            pronunciacionFonetica = "nak.sa",
            pronunciacionVerificada = true
        )
        val remoto = RemotoPrueba()
        val engine = engine(storage = StoragePrueba(), remoto = remoto, conectado = true)

        val resultado = engine.resolver(SolicitudPronunciacion(palabra.texto, palabra.idioma))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
        assertEquals(0, remoto.llamadas)
    }

    @Test
    fun otroIdiomaPuedeUsarProveedorRemoto() = runBlocking {
        val remoto = RemotoPrueba(byteArrayOf(1, 2, 3))
        val storage = StoragePrueba()
        val engine = engine(storage = storage, remoto = remoto, conectado = true)

        val resultado = engine.resolver(SolicitudPronunciacion("Hola", espanol))

        assertTrue(resultado is ResultadoPronunciacion.AudioRemoto)
        assertEquals(1, remoto.llamadas)
        assertEquals(1, storage.guardados)
    }

    @Test
    fun cacheLocalTienePrioridadSobreRed() = runBlocking {
        val remoto = RemotoPrueba()
        val local = ResultadoPronunciacion.AudioCacheado(byteArrayOf(7))
        val engine = engine(StoragePrueba(local), remoto, conectado = true)

        val resultado = engine.resolver(SolicitudPronunciacion("Hola", espanol))

        assertTrue(resultado is ResultadoPronunciacion.AudioCacheado)
        assertEquals(0, remoto.llamadas)
    }

    @Test
    fun ausenciaDeConexionSeManejaSinCrash() = runBlocking {
        val remoto = RemotoPrueba()
        val engine = engine(StoragePrueba(), remoto, conectado = false)

        val resultado = engine.resolver(SolicitudPronunciacion("Hola", espanol))

        assertEquals(ResultadoPronunciacion.NoDisponible, resultado)
        assertEquals(0, remoto.llamadas)
    }

    private fun engine(
        storage: PronunciationStorage = StoragePrueba(),
        remoto: RemotoPrueba = RemotoPrueba(),
        conectado: Boolean = true,
        audioRepository: AudioPronunciacionRepository = AudioRepositoryPrueba()
    ) = PronunciationEngineImpl(
        storage = storage,
        audioRepository = audioRepository,
        vozRepository = remoto,
        networkAvailability = NetworkAvailability { conectado }
    )

    private fun audio(
        palabraId: Int,
        idioma: Idioma,
        origen: OrigenAudioPronunciacion,
        referencia: String = "audio",
        verificado: Boolean = true
    ) = AudioPronunciacion(
        palabraId = palabraId,
        idioma = idioma,
        referencia = ReferenciaAudioPronunciacion.Asset(referencia),
        verificado = verificado,
        origen = origen
    )

    private class AudioRepositoryPrueba(
        private val audios: List<AudioPronunciacion> = emptyList(),
        private val contenidos: Map<String, ByteArray> = mapOf("audio" to byteArrayOf(1)),
        private val porTexto: Map<String, List<AudioPronunciacion>> = emptyMap()
    ) : AudioPronunciacionRepository {
        override suspend fun buscarHumanosPorTexto(texto: String, idioma: Idioma): List<AudioPronunciacion> =
            porTexto[normalizarFormaAudio(texto)].orEmpty()

        override suspend fun buscarVerificados(
            palabraId: Int,
            idioma: Idioma
        ): List<AudioPronunciacion> = audios

        override suspend fun registrar(audio: AudioPronunciacion) = Unit

        override suspend fun estaDisponibleOffline(audio: AudioPronunciacion): Boolean =
            audio.referencia.valor in contenidos

        override suspend fun leerAudio(audio: AudioPronunciacion): ByteArray? =
            contenidos[audio.referencia.valor]
    }

    private class StoragePrueba(
        private val resultado: ResultadoPronunciacion? = null
    ) : PronunciationStorage {
        var guardados = 0

        override suspend fun leer(solicitud: SolicitudPronunciacion): ResultadoPronunciacion? = resultado

        override suspend fun guardar(solicitud: SolicitudPronunciacion, audio: ByteArray) {
            guardados++
        }
    }

    private class RemotoPrueba(
        private val audio: ByteArray = byteArrayOf(9)
    ) : VozRepository {
        var llamadas = 0

        override suspend fun sintetizarVoz(texto: String, voiceId: String?): ByteArray {
            llamadas++
            return audio
        }
    }
}
