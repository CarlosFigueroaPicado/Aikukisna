package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.repository.CulturaRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsultarCulturaTukiUseCaseTest {
    private val fuente = FuenteDocumento(5, "Documento cultural", "Autor", 1980, null)
    private val contenido = CulturaContenido(
        id = 1,
        titulo = "Vestuario",
        contenido = "Los miskitos usaban prendas elaboradas con materiales disponibles en su región.",
        rangoPaginaInicio = 59,
        rangoPaginaFin = 62,
        fuente = fuente
    )
    private val caso = ConsultarCulturaTukiUseCase(RepositorioCultura(listOf(contenido)))

    @Test
    fun respondePreguntaCulturalConContenidoYFuenteVerificados() {
        val respuesta = ejecutarCultura { caso("¿Cómo se vestían los miskitos?", 1) }

        assertTrue(respuesta!!.contains("Vestuario"))
        assertTrue(respuesta.contains("Documento cultural"))
        assertTrue(respuesta.contains("pág. 59–62"))
    }

    @Test
    fun noEntregaCulturaDeOtroIdioma() {
        val respuesta = ejecutarCultura { caso("¿Cómo es la cultura kriol?", 3) }

        assertEquals(
            "Todavía no tengo contenido cultural verificado para responder esa pregunta.",
            respuesta
        )
    }

    @Test
    fun reconoceUnTemaCulturalEscritoDirectamente() {
        val respuesta = ejecutarCultura { caso("Vestuario", 1) }

        assertTrue(respuesta!!.contains("Vestuario"))
        assertTrue(respuesta.contains("Documento cultural"))
    }

    @Test
    fun noInterfiereConPreguntasSobreLasCapacidadesDeTuki() {
        assertEquals(null, ejecutarCultura { caso("¿Qué puedes hacer?", 1) })
        assertEquals(null, ejecutarCultura { caso("¿Qué más puedes hacer?", 1) })
        assertEquals(null, ejecutarCultura { caso("¿Me puedes ayudar con las lecciones?", 1) })
    }

    private class RepositorioCultura(
        private val contenido: List<CulturaContenido>
    ) : CulturaRepository {
        override suspend fun obtenerContenidoCultural(): List<CulturaContenido> = contenido
        override suspend fun obtenerContenidoCulturalPorId(id: Int): CulturaContenido? =
            contenido.firstOrNull { it.id == id }
    }
}

private fun <T> ejecutarCultura(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) {
            resultado = result
        }
    })
    return resultado!!.getOrThrow()
}
