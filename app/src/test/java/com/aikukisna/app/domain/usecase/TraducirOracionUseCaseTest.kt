package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.EstadoValidacion
import com.aikukisna.app.domain.conocimiento.OpcionTraduccion
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import com.aikukisna.app.domain.repository.TraductorAutomaticoLocal
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TraducirOracionUseCaseTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val ingles = Idioma(4, "en", "Inglés Estándar")
    private val fuente = FuenteDocumento(1, "Fuente documentada", null, null, null)

    private val conocimiento = ConocimientoPrueba(
        expresiones = mapOf("como estas" to "Nahki sma"),
        palabras = mapOf("casa" to "utla", "grande" to "tara", "buenos dias" to "Good morning")
    )

    @Test
    fun priorizaLaFraseVerificada() {
        val resultado = ejecutarOracion {
            TraducirOracionUseCase(TraducirTextoUseCase(conocimiento), SinModelo)("como estas", espanol.id, miskito.id)
        }
        assertEquals("Nahki sma", resultado.texto)
        assertEquals(TipoTraduccion.VERIFICADA, resultado.tipo)
    }

    @Test
    fun componeLiteralmenteYMarcaLasPalabrasDesconocidas() {
        val resultado = ejecutarOracion {
            TraducirOracionUseCase(TraducirTextoUseCase(conocimiento), SinModelo)("La casa grande", espanol.id, miskito.id)
        }
        assertEquals("[La] utla tara", resultado.texto)
        assertEquals(TipoTraduccion.LITERAL, resultado.tipo)
        assertTrue(resultado.nota!!.contains("La"))
    }

    @Test
    fun usaElModeloLocalEntreEspanolEInglesConGlosario() {
        val modelo = ModeloPrueba()
        val resultado = ejecutarOracion {
            TraducirOracionUseCase(TraducirTextoUseCase(conocimiento), modelo)("buenos dias amigo", espanol.id, ingles.id)
        }
        assertEquals("Good morning, friend", resultado.texto)
        assertEquals(TipoTraduccion.AUTOMATICA, resultado.tipo)
        assertEquals(FuenteTraduccion.IA, resultado.fuente)
        assertEquals(listOf("buenos dias = Good morning"), modelo.glosarioRecibido)
    }

    @Test
    fun noUsaElModeloParaMiskito() {
        val modelo = ModeloPrueba()
        ejecutarOracion {
            TraducirOracionUseCase(TraducirTextoUseCase(conocimiento), modelo)("casa grande", espanol.id, miskito.id)
        }
        assertEquals(null, modelo.glosarioRecibido)
    }

    @Test
    fun fallaSiNoReconoceNingunaPalabra() {
        assertThrows(IllegalStateException::class.java) {
            ejecutarOracion {
                TraducirOracionUseCase(TraducirTextoUseCase(conocimiento), SinModelo)("ornitorrinco azul", espanol.id, miskito.id)
            }
        }
    }

    private object SinModelo : TraductorAutomaticoLocal {
        override fun disponible() = false
        override fun soporta(idiomaOrigenId: Int, idiomaDestinoId: Int) = false
        override suspend fun traducir(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int, glosario: List<String>) =
            error("No disponible")
    }

    private class ModeloPrueba : TraductorAutomaticoLocal {
        var glosarioRecibido: List<String>? = null
        override fun disponible() = true
        override fun soporta(idiomaOrigenId: Int, idiomaDestinoId: Int) =
            setOf(idiomaOrigenId, idiomaDestinoId) == setOf(2, 4)
        override suspend fun traducir(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int, glosario: List<String>): String {
            glosarioRecibido = glosario
            return "Good morning, friend"
        }
    }

    private inner class ConocimientoPrueba(
        private val expresiones: Map<String, String>,
        private val palabras: Map<String, String>
    ) : RepositorioConocimiento {
        override suspend fun buscarPalabra(texto: String, idiomaId: Int) = ResultadoBusquedaConocimiento.NoEncontrada
        override suspend fun obtenerEntrada(palabraId: Int, idiomaDestinoId: Int?): EntradaConocimiento? = null
        override suspend fun resolverTraduccion(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): ResolucionTraduccion {
            val clave = texto.lowercase()
            expresiones[clave]?.let { return ResolucionTraduccion.Expresion(it, EstadoValidacion.VALIDADA) }
            palabras[clave]?.let {
                return ResolucionTraduccion.Unica(OpcionTraduccion(Palabra(99, miskito, it, null, fuente)))
            }
            return ResolucionTraduccion.NoEncontrada
        }
    }
}

private fun <T> ejecutarOracion(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) { resultado = result }
    })
    return resultado!!.getOrThrow()
}
