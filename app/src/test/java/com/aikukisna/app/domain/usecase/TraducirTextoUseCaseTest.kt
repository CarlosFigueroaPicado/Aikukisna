package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TraducirTextoUseCaseTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val fuente = FuenteDocumento(1, "Diccionario verificado", null, null, null)
    private val tingki = Palabra(10, miskito, "Tingki", null, fuente)
    private val gracias = Palabra(20, espanol, "Gracias", null, fuente)
    private val traduccion = Traduccion(1, tingki, gracias, null)

    @Test
    fun traduceEspanolAMiskitoConDatoVerificado() {
        val resultado = ejecutarSuspend {
            TraducirTextoUseCase(RepositorioPrueba(gracias, traduccion))(
                "gracias", espanol.id, miskito.id
            )
        }

        assertEquals("Tingki", resultado.texto)
        assertEquals(FuenteTraduccion.DICCIONARIO, resultado.fuente)
    }

    @Test
    fun traduceMiskitoAEspanolConDatoVerificado() {
        val resultado = ejecutarSuspend {
            TraducirTextoUseCase(RepositorioPrueba(tingki, traduccion))(
                "Tingki", miskito.id, espanol.id
            )
        }

        assertEquals("Gracias", resultado.texto)
        assertEquals(FuenteTraduccion.DICCIONARIO, resultado.fuente)
    }

    @Test
    fun rechazaTextoSinTraduccionVerificada() {
        val error = assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(RepositorioPrueba(null, null))(
                    "ornitorrinco", espanol.id, miskito.id
                )
            }
        }

        assertEquals("No hay una traducción verificada para este texto", error.message)
    }

    @Test
    fun noUsaVocabularioCodificadoSiLaFuenteNoContieneHola() {
        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(RepositorioPrueba(null, null))(
                    "hola", espanol.id, miskito.id
                )
            }
        }
    }

    @Test
    fun noUsaVocabularioCodificadoSiLaFuenteNoContieneNaksa() {
        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(RepositorioPrueba(null, null))(
                    "Naksa", miskito.id, espanol.id
                )
            }
        }
    }

    @Test
    fun noComponeUnSaludoSinUnaExpresionCompleta() {
        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(RepositorioPrueba(null, null))(
                    "Hola, ¿cómo estás?", espanol.id, miskito.id
                )
            }
        }
    }

    @Test
    fun traduceOracionCompletaVerificada() {
        val resultado = ejecutarSuspend {
            TraducirTextoUseCase(
                RepositorioPrueba(
                    palabra = null,
                    traduccion = null,
                    oraciones = mapOf(
                        Triple("¿Cómo estás?", espanol.id, miskito.id) to "¿Nahki sma?"
                    )
                )
            )("¿Cómo estás?", espanol.id, miskito.id)
        }

        assertEquals("¿Nahki sma?", resultado.texto)
        assertEquals(FuenteTraduccion.DICCIONARIO, resultado.fuente)
    }

    @Test
    fun noInventaUnaTraduccionParaTextoNuevo() {
        val error = assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(RepositorioPrueba(null, null))(
                    "Voy a la escuela.", espanol.id, miskito.id
                )
            }
        }

        assertEquals("No hay una traducción verificada para este texto", error.message)
    }

    @Test
    fun noConstruyeUnaOracionSustituyendoUnaPlantilla() {
        val sustantivo = Categoria(1, "Sustantivo")
        val hombre = Palabra(101, espanol, "hombre", sustantivo, fuente)
        val nino = Palabra(102, espanol, "niño", sustantivo, fuente)
        val waitna = Palabra(201, miskito, "waitna", sustantivo, fuente)
        val tuktan = Palabra(202, miskito, "tuktan", sustantivo, fuente)
        val oracion = OracionEjemplo(
            id = 1,
            textoOrigen = "Yo vi al hombre",
            textoDestino = "Yang waitna ba kaikri",
            idiomaOrigenId = espanol.id,
            idiomaDestinoId = miskito.id,
            leccion = null,
            fuente = fuente
        )
        val repositorio = RepositorioPlantillaPrueba(
            palabras = listOf(hombre, nino, waitna, tuktan),
            traducciones = listOf(
                Traduccion(1, hombre, waitna, null),
                Traduccion(2, nino, tuktan, null)
            ),
            oraciones = listOf(oracion)
        )

        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(repositorio)("Yo vi al niño", espanol.id, miskito.id)
            }
        }
    }

    @Test
    fun noComponeUnaFraseTraduciendoTokensPorSeparado() {
        val hola = Palabra(301, espanol, "hola", null, fuente)
        val graciasLocal = Palabra(302, espanol, "gracias", null, fuente)
        val naksa = Palabra(401, miskito, "naksa", null, fuente)
        val tingkiLocal = Palabra(402, miskito, "tingki", null, fuente)
        val repositorio = RepositorioPlantillaPrueba(
            palabras = listOf(hola, graciasLocal, naksa, tingkiLocal),
            traducciones = listOf(
                Traduccion(11, hola, naksa, null),
                Traduccion(12, graciasLocal, tingkiLocal, null)
            ),
            oraciones = emptyList()
        )

        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(repositorio)("Hola gracias!", espanol.id, miskito.id)
            }
        }
    }

    @Test
    fun noUsaUnIdiomaPuenteSinUnaRelacionDirecta() {
        val ingles = Idioma(4, "en", "Inglés Estándar")
        val kriol = Idioma(3, "bzk", "Inglés Kriol")
        val goodMorning = Palabra(501, ingles, "Good morning", null, fuente)
        val buenosDias = Palabra(502, espanol, "Buenos días", null, fuente)
        val maanin = Palabra(503, kriol, "Maanin", null, fuente)
        val repositorio = RepositorioPlantillaPrueba(
            palabras = listOf(goodMorning, buenosDias, maanin),
            traducciones = listOf(
                Traduccion(21, goodMorning, buenosDias, null),
                Traduccion(22, buenosDias, maanin, null)
            ),
            oraciones = emptyList()
        )

        assertThrows(IllegalStateException::class.java) {
            ejecutarSuspend {
                TraducirTextoUseCase(repositorio)("Good morning", ingles.id, kriol.id)
            }
        }
    }

    private class RepositorioPrueba(
        private val palabra: Palabra?,
        private val traduccion: Traduccion?,
        private val oraciones: Map<Triple<String, Int, Int>, String> = emptyMap()
    ) : DiccionarioRepository {
        override suspend fun buscarPalabras(
            query: String,
            idiomaId: Int,
            limite: Int,
            offset: Int,
            categoriaId: Int?
        ): List<Palabra> = palabra?.let { listOf(it) }.orEmpty()

        override suspend fun obtenerPalabraPorId(id: Int): Palabra? = palabra

        override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> {
            val valor = traduccion ?: return emptyList()
            return if (valor.palabraOrigen.id == palabraId) {
                listOf(valor)
            } else {
                listOf(valor.copy(palabraOrigen = valor.palabraDestino, palabraDestino = valor.palabraOrigen))
            }
        }


        override suspend fun buscarOracionExacta(
            texto: String,
            idiomaOrigenId: Int,
            idiomaDestinoId: Int
        ): String? = oraciones[Triple(texto, idiomaOrigenId, idiomaDestinoId)]

        override suspend fun obtenerOracionesPorIdiomas(
            idiomaOrigenId: Int,
            idiomaDestinoId: Int
        ): List<OracionEjemplo> = emptyList()
    }

    private class RepositorioPlantillaPrueba(
        private val palabras: List<Palabra>,
        private val traducciones: List<Traduccion>,
        private val oraciones: List<OracionEjemplo>
    ) : DiccionarioRepository {
        override suspend fun buscarPalabras(
            query: String,
            idiomaId: Int,
            limite: Int,
            offset: Int,
            categoriaId: Int?
        ): List<Palabra> = palabras.filter {
            it.idioma.id == idiomaId && it.texto.equals(query, ignoreCase = true)
        }

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
        ): List<OracionEjemplo> = oraciones
    }

}

private fun <T> ejecutarSuspend(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(value: Result<T>) {
            resultado = value
        }
    })
    return resultado!!.getOrThrow()
}
