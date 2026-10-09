package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.EstadoValidacion
import com.aikukisna.app.domain.conocimiento.OpcionTraduccion
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.conocimiento.SegmentoTraduccion
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class PoliticaTraduccionConocimientoTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val fuente = FuenteDocumento(1, "Fuente documentada", null, null, null)
    private val casa = Palabra(1, espanol, "casa", null, fuente)
    private val utla = Palabra(2, miskito, "utla", null, fuente)
    private val watla = Palabra(3, miskito, "watla", null, fuente)

    @Test
    fun usaUnaExpresionCompletaRegistrada() {
        val caso = TraducirTextoUseCase(RepositorioPrueba(ResolucionTraduccion.Expresion("Nahki sma?", EstadoValidacion.IMPORTADA)))

        val resultado = ejecutarConocimiento { caso("¿Cómo estás?", espanol.id, miskito.id) }

        assertEquals("Nahki sma?", resultado.texto)
    }

    @Test
    fun conservaLasOpcionesCuandoLaTraduccionEsAmbigua() {
        val caso = TraducirTextoUseCase(
            RepositorioPrueba(
                ResolucionTraduccion.Ambigua(
                    listOf(
                        OpcionTraduccion(utla, contexto = "vivienda", estado = EstadoValidacion.VALIDADA),
                        OpcionTraduccion(watla, contexto = "edificio", estado = EstadoValidacion.DOCUMENTADA)
                    )
                )
            )
        )

        val resultado = ejecutarConocimiento { caso("casa", espanol.id, miskito.id) }

        assertEquals(2, resultado.alternativas.size)
        assertTrue(resultado.alternativas.any { it.contains("vivienda") })
        assertTrue(resultado.alternativas.any { it.contains("edificio") })
    }

    @Test
    fun aceptaLaPalabraCanonicaResueltaDesdeUnaVarianteConocida() {
        val repositorio = RepositorioPrueba(
            ResolucionTraduccion.Unica(OpcionTraduccion(utla, estado = EstadoValidacion.DOCUMENTADA)),
            ResultadoBusquedaConocimiento.Exacta(casa, medianteVariante = "casita")
        )

        val busqueda = ejecutarConocimiento { repositorio.buscarPalabra("casita", espanol.id) }
        val resultado = ejecutarConocimiento { TraducirTextoUseCase(repositorio)("casita", espanol.id, miskito.id) }

        assertTrue(busqueda is ResultadoBusquedaConocimiento.Exacta && busqueda.medianteVariante == "casita")
        assertEquals("utla", resultado.texto)
    }

    @Test
    fun rechazaUnaComposicionAunqueSusSegmentosEstenDocumentados() {
        val caso = TraducirTextoUseCase(
            RepositorioPrueba(
                ResolucionTraduccion.ComposicionDocumentada(
                    listOf(
                        SegmentoTraduccion("buenos días", "titan yamni", EstadoValidacion.DOCUMENTADA),
                        SegmentoTraduccion("amigo", "amigo validado", EstadoValidacion.VALIDADA)
                    )
                )
            )
        )

        val error = assertThrows(IllegalStateException::class.java) {
            ejecutarConocimiento { caso("buenos días amigo", espanol.id, miskito.id) }
        }
        assertTrue(error.message.orEmpty().contains("No hay una expresión completa verificada"))
    }

    private class RepositorioPrueba(
        private val resolucion: ResolucionTraduccion,
        private val busqueda: ResultadoBusquedaConocimiento = ResultadoBusquedaConocimiento.NoEncontrada
    ) : RepositorioConocimiento {
        override suspend fun buscarPalabra(texto: String, idiomaId: Int) = busqueda

        override suspend fun obtenerEntrada(palabraId: Int, idiomaDestinoId: Int?): EntradaConocimiento? = null

        override suspend fun resolverTraduccion(
            texto: String,
            idiomaOrigenId: Int,
            idiomaDestinoId: Int
        ) = resolucion
    }
}

private fun <T> ejecutarConocimiento(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) {
            resultado = result
        }
    })
    return resultado!!.getOrThrow()
}
