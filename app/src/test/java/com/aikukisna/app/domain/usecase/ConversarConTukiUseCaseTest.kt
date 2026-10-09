package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversarConTukiUseCaseTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val fuente = FuenteDocumento(1, "Fuente verificada", null, null, null)
    private val naksa = Palabra(10, miskito, "Naksa", null, fuente)
    private val hola = Palabra(20, espanol, "Hola", null, fuente)
    private val repositorio = RepositorioTuki(
        listOf(naksa, hola),
        listOf(Traduccion(1, hola, naksa, null))
    )

    @Test
    fun aceptaFormasDistintasDePedirUnaTraduccion() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "Traduce hola al miskito")),
                miskito.id
            )
        }

        assertEquals(
            "En Miskito, encontré esta traducción verificada para “hola”:\n• Naksa\n¿Quieres practicar otra palabra o frase?",
            respuesta
        )
    }

    @Test
    fun respondeSignificadoSoloConElDatoVerificado() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "¿Cuál es el significado de Naksa?")),
                miskito.id
            )
        }

        assertEquals(
            "Encontré 1 significado verificado para “Naksa”:\n• Hola\n¿Quieres consultar otra palabra?",
            respuesta
        )
    }

    @Test
    fun noGeneraContenidoFueraDeLaFuenteVerificada() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "Cuéntame sobre el miskito")),
                miskito.id
            )
        }

        assertEquals(
            TukiOfflineResponder.MENSAJE_GUIA,
            respuesta
        )
    }

    @Test
    fun noUsaEspanolComoPuenteSinRelacionDirectaVerificada() {
        val ingles = Idioma(4, "en", "Inglés Estándar")
        val kriol = Idioma(3, "bzk", "Inglés Kriol")
        val goodMorning = Palabra(30, ingles, "Good morning", null, fuente)
        val buenosDias = Palabra(31, espanol, "Buenos días", null, fuente)
        val maanin = Palabra(32, kriol, "Maanin", null, fuente)
        val repositorioMultilingue = RepositorioTuki(
            listOf(goodMorning, buenosDias, maanin),
            listOf(
                Traduccion(2, goodMorning, buenosDias, null),
                Traduccion(3, buenosDias, maanin, null)
            )
        )

        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorioMultilingue)(
                listOf(MensajeChat(RolChat.USUARIO, "Traduce Good morning al inglés kriol")),
                miskito.id
            )
        }

        assertEquals(
            "Busqué “Good morning” en el contenido local, pero no encontré una equivalencia verificada. " +
                "Puedes escribir solo la palabra o indicar el idioma; por ejemplo: “¿qué significa naksa?” o “¿cómo se dice casa en miskito?”.",
            respuesta
        )
    }

    @Test
    fun entiendeUnaPalabraEscritaSinComando() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "Naksa")),
                miskito.id
            )
        }

        assertEquals(
            "Encontré estas equivalencias verificadas para “Naksa”:\n" +
                "• Naksa (Miskito) = Hola (Español)\n" +
                "Puedes preguntarme por otra palabra, una frase, una lección o un tema cultural.",
            respuesta
        )
    }

    @Test
    fun mantieneElHiloCuandoElEstudianteRespondeSi() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(
                    MensajeChat(RolChat.TUKI, "¿Quieres consultar otra palabra?"),
                    MensajeChat(RolChat.USUARIO, "Sí")
                ),
                miskito.id
            )
        }

        assertEquals(
            "Perfecto. Escribe la palabra que quieres consultar y buscaré sus equivalencias verificadas.",
            respuesta
        )
    }

    @Test
    fun entiendeLaPeticionAunqueTengaPalabrasDeCortesia() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "Por favor, dime cómo se dice hola al miskito")),
                miskito.id
            )
        }

        assertEquals(
            "En Miskito, encontré esta traducción verificada para “hola”:\n• Naksa\n¿Quieres practicar otra palabra o frase?",
            respuesta
        )
    }

    @Test
    fun explicaSusCapacidadesConLasPreguntasDelEstudiante() {
        listOf("¿Qué puedes hacer?", "¿Qué más puedes hacer?").forEach { pregunta ->
            val respuesta = ejecutarTuki {
                ConversarConTukiUseCase(repositorio)(
                    listOf(MensajeChat(RolChat.USUARIO, pregunta)),
                    miskito.id
                )
            }

            assertEquals(
                "¡Puedo acompañarte como tu maestro interino de Miskito! " +
                    "Sin conexión puedo buscar palabras y expresiones verificadas, traducir el contenido disponible entre español y Miskito, " +
                    "mostrar pronunciaciones que hayan sido validadas, ayudarte a continuar tus lecciones, revisar tu progreso y consultar la información cultural guardada en la aplicación. " +
                    "También recuerdo el tema de nuestra conversación para que podamos avanzar paso a paso. ¿Quieres comenzar con una palabra, una lección o un tema cultural?",
                respuesta
            )
        }
    }

    @Test
    fun entiendeCuandoElEstudiantePideAyudaConLasLecciones() {
        val respuesta = ejecutarTuki {
            ConversarConTukiUseCase(repositorio)(
                listOf(MensajeChat(RolChat.USUARIO, "¿Me puedes ayudar con las lecciones?")),
                miskito.id
            )
        }

        assertEquals(
            "Claro. Puedo ayudarte a continuar tus lecciones, explicarte qué estudiar y practicar el vocabulario verificado. Dime “quiero aprender” para revisar la siguiente lección disponible.",
            respuesta
        )
    }

    private class RepositorioTuki(
        private val palabras: List<Palabra>,
        private val traducciones: List<Traduccion>
    ) : DiccionarioRepository {
        override suspend fun buscarPalabras(query: String, idiomaId: Int, limite: Int, offset: Int, categoriaId: Int?): List<Palabra> =
            palabras.filter { it.idioma.id == idiomaId && it.texto.equals(query, ignoreCase = true) }

        override suspend fun obtenerPalabraPorId(id: Int): Palabra? = palabras.firstOrNull { it.id == id }

        override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> = traducciones.mapNotNull {
            when (palabraId) {
                it.palabraOrigen.id -> it
                it.palabraDestino.id -> it.copy(palabraOrigen = it.palabraDestino, palabraDestino = it.palabraOrigen)
                else -> null
            }
        }

        override suspend fun buscarOracionExacta(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): String? = null

        override suspend fun obtenerOracionesPorIdiomas(idiomaOrigenId: Int, idiomaDestinoId: Int): List<OracionEjemplo> = emptyList()
    }
}

private fun <T> ejecutarTuki(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(value: Result<T>) {
            resultado = value
        }
    })
    return resultado!!.getOrThrow()
}
