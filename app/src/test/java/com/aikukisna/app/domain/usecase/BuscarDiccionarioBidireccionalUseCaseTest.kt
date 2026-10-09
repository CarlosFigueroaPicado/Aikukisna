package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.OpcionTraduccion
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuscarDiccionarioBidireccionalUseCaseTest {
    private val espanol = idioma("es")
    private val miskito = idioma("mi")
    private val kriol = idioma("bzk")
    private val ingles = idioma("en")

    @Test fun buscaEspanolHaciaMiskitoSinSelectorManual() {
        val hola = palabra(10, espanol, "hola")
        val naksa = palabra(20, miskito, "naksa")
        val resultado = ejecutar {
            BuscarDiccionarioBidireccionalUseCase(FalsoRepositorio(mapOf(("hola" to espanol.id) to entrada(hola, naksa))))(
                "Hola!", espanol, miskito
            )
        }
        val exactas = resultado as ResultadoDiccionarioBidireccional.Exactas
        assertEquals("naksa", exactas.entradas.single().traducciones.single().palabra.texto)
    }

    @Test fun buscaMiskitoHaciaEspanol() {
        val naksa = palabra(20, miskito, "naksa")
        val hola = palabra(10, espanol, "hola")
        val resultado = ejecutar {
            BuscarDiccionarioBidireccionalUseCase(FalsoRepositorio(mapOf(("naksa" to miskito.id) to entrada(naksa, hola))))(
                "naksa", espanol, miskito
            )
        }
        assertEquals("hola", (resultado as ResultadoDiccionarioBidireccional.Exactas)
            .entradas.single().traducciones.single().palabra.texto)
    }

    @Test fun conservaAmbasInterpretacionesCuandoElTextoExisteEnAmbosIdiomas() {
        val base = palabra(1, espanol, "pan")
        val meta = palabra(2, miskito, "pan")
        val repositorio = FalsoRepositorio(
            mapOf(
                ("pan" to espanol.id) to entrada(base, palabra(3, miskito, "tawa")),
                ("pan" to miskito.id) to entrada(meta, palabra(4, espanol, "recipiente"))
            )
        )
        val resultado = ejecutar { BuscarDiccionarioBidireccionalUseCase(repositorio)("pan", espanol, miskito) }
        assertEquals(2, (resultado as ResultadoDiccionarioBidireccional.Exactas).entradas.size)
    }

    @Test fun usaElIdiomaMetaKriolEInglesPorCodigoSinIdsNuevos() {
        listOf(kriol, ingles).forEach { meta ->
            val hola = palabra(10, espanol, "hola")
            val destino = palabra(20 + meta.id, meta, "saludo-${meta.codigo}")
            val resultado = ejecutar {
                BuscarDiccionarioBidireccionalUseCase(FalsoRepositorio(mapOf(("hola" to espanol.id) to entrada(hola, destino))))(
                    "hola", espanol, meta
                )
            }
            assertEquals(meta.id, (resultado as ResultadoDiccionarioBidireccional.Exactas)
                .entradas.single().traducciones.single().palabra.idioma.id)
        }
    }

    @Test fun unaSugerenciaNoSeConvierteEnTraduccion() {
        val sugerida = palabra(10, espanol, "hola")
        val repositorio = FalsoRepositorio(
            exactas = emptyMap(),
            sugerencias = mapOf(("olaa" to espanol.id) to listOf(sugerida))
        )
        val resultado = ejecutar { BuscarDiccionarioBidireccionalUseCase(repositorio)("olaa", espanol, miskito) }
        assertTrue(resultado is ResultadoDiccionarioBidireccional.Sugerencias)
    }

    private class FalsoRepositorio(
        private val exactas: Map<Pair<String, Int>, EntradaConocimiento>,
        private val sugerencias: Map<Pair<String, Int>, List<Palabra>> = emptyMap()
    ) : RepositorioConocimiento {
        private val entradas = exactas.values.associateBy { it.palabra.id }

        override suspend fun buscarPalabra(texto: String, idiomaId: Int): ResultadoBusquedaConocimiento {
            val clave = texto.trim().trim('!', '¡').lowercase() to idiomaId
            exactas[clave]?.let { return ResultadoBusquedaConocimiento.Exacta(it.palabra) }
            sugerencias[clave]?.let { return ResultadoBusquedaConocimiento.Sugerencias(it) }
            return ResultadoBusquedaConocimiento.NoEncontrada
        }

        override suspend fun obtenerEntrada(palabraId: Int, idiomaDestinoId: Int?): EntradaConocimiento? =
            entradas[palabraId]?.let { entrada ->
                entrada.copy(traducciones = entrada.traducciones.filter { idiomaDestinoId == null || it.palabra.idioma.id == idiomaDestinoId })
            }

        override suspend fun resolverTraduccion(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int) =
            ResolucionTraduccion.NoEncontrada
    }

    private fun idioma(codigo: String) = Idioma.DISPONIBLES.first { it.codigo == codigo }
    private fun palabra(id: Int, idioma: Idioma, texto: String) = Palabra(
        id = id,
        idioma = idioma,
        texto = texto,
        categoria = null,
        fuente = FuenteDocumento(1, "Corpus de prueba", null, null, null)
    )
    private fun entrada(origen: Palabra, destino: Palabra) = EntradaConocimiento(
        palabra = origen,
        acepciones = emptyList(),
        traducciones = listOf(OpcionTraduccion(destino)),
        ejemplos = emptyList()
    )
}

private fun <T> ejecutar(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) { resultado = result }
    })
    return resultado!!.getOrThrow()
}
