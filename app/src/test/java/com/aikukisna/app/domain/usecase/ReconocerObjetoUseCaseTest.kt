package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.IaRepository
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.ReconocedorObjetosLocal
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconocerObjetoUseCaseTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val ingles = Idioma(4, "en", "Inglés Estándar")
    private val fuente = FuenteDocumento(1, "Diccionario verificado", null, null, null)
    private val agua = Palabra(2088, espanol, "agua", null, fuente)
    private val li = Palabra(2089, miskito, "lî", null, fuente)
    private val water = Palabra(2090, ingles, "water", null, fuente)
    private val repositorio = RepositorioDiccionarioImagen(
        palabras = listOf(agua, li, water),
        traducciones = listOf(
            Traduccion(1643, agua, li, null),
            Traduccion(1644, agua, water, null)
        )
    )

    @Test
    fun conConexionPriorizaReconocimientoEnLineaYValidaLaTraduccionLocalmente() {
        var usoReconocedorLocal = false
        val casoDeUso = ReconocerObjetoUseCase(
            diccionarioRepository = repositorio,
            traducirTextoUseCase = TraducirTextoUseCase(repositorio),
            iaRepository = IaImagenPrueba("agua."),
            conectividad = NetworkAvailability { true },
            reconocedorObjetosLocal = ReconocedorObjetosLocal {
                usoReconocedorLocal = true
                listOf("Musical instrument")
            }
        )

        val resultado = ejecutarReconocimiento { casoDeUso("imagen", miskito.id) }

        assertEquals("agua", resultado.objetoDetectado)
        assertEquals("lî", resultado.traduccion)
        assertFalse(usoReconocedorLocal)
    }

    @Test
    fun usaMlKitComoRespaldoCuandoFallaElReconocimientoEnLinea() {
        var usoGemini = false
        val casoDeUso = ReconocerObjetoUseCase(
            diccionarioRepository = repositorio,
            traducirTextoUseCase = TraducirTextoUseCase(repositorio),
            iaRepository = IaImagenPrueba(
                error = IllegalStateException("Sin respuesta"),
                alConsultar = { usoGemini = true }
            ),
            conectividad = NetworkAvailability { true },
            reconocedorObjetosLocal = ReconocedorObjetosLocal { listOf("Water") }
        )

        val resultado = ejecutarReconocimiento { casoDeUso("imagen", miskito.id) }

        assertEquals("agua", resultado.objetoDetectado)
        assertEquals("lî", resultado.traduccion)
        assertTrue(usoGemini)
    }

    @Test
    fun usaMlKitSinConsultarGeminiCuandoNoHayConexion() {
        var usoGemini = false
        val casoDeUso = ReconocerObjetoUseCase(
            diccionarioRepository = repositorio,
            traducirTextoUseCase = TraducirTextoUseCase(repositorio),
            iaRepository = IaImagenPrueba("agua", alConsultar = { usoGemini = true }),
            conectividad = NetworkAvailability { false },
            reconocedorObjetosLocal = ReconocedorObjetosLocal { listOf("Water") }
        )

        val resultado = ejecutarReconocimiento { casoDeUso("imagen", miskito.id) }

        assertEquals("agua", resultado.objetoDetectado)
        assertEquals("lî", resultado.traduccion)
        assertFalse(usoGemini)
    }

    private class IaImagenPrueba(
        private val respuesta: String = "",
        private val error: Exception? = null,
        private val alConsultar: () -> Unit = {}
    ) : IaRepository {
        override suspend fun preguntar(prompt: String): String = error("No se usa en esta prueba")

        override suspend fun conversar(historial: List<MensajeChat>, contexto: String?): String =
            error("No se usa en esta prueba")

        override suspend fun preguntarConImagen(
            prompt: String,
            imagenBase64: String,
            mimeType: String
        ): String {
            alConsultar()
            error?.let { throw it }
            return respuesta
        }

        override suspend fun preguntarConAudio(
            prompt: String,
            audioBase64: String,
            audioMimeType: String
        ): String = error("No se usa en esta prueba")
    }
}

internal class RepositorioDiccionarioImagen(
    private val palabras: List<Palabra>,
    private val traducciones: List<Traduccion>
) : DiccionarioRepository {
    override suspend fun buscarPalabras(
        query: String,
        idiomaId: Int,
        limite: Int,
        offset: Int,
        categoriaId: Int?
    ): List<Palabra> = palabras.filter {
        it.idioma.id == idiomaId && it.texto.contains(query, ignoreCase = true)
    }.drop(offset).take(limite)

    override suspend fun obtenerPalabraPorId(id: Int): Palabra? = palabras.firstOrNull { it.id == id }

    override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> = traducciones.mapNotNull {
        when (palabraId) {
            it.palabraOrigen.id -> it
            it.palabraDestino.id -> it.copy(
                palabraOrigen = it.palabraDestino,
                palabraDestino = it.palabraOrigen
            )
            else -> null
        }
    }

    override suspend fun buscarOracionExacta(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): String? = null

    override suspend fun obtenerOracionesPorIdiomas(
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): List<OracionEjemplo> = emptyList()
}

internal fun <T> ejecutarReconocimiento(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) {
            resultado = result
        }
    })
    return resultado!!.getOrThrow()
}
