package com.aikukisna.app.domain.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdiomasTukiTest {
    private val classifier = TukiIntentClassifier()

    @Test fun distingueKriolDeInglesEstandar() {
        assertEquals(setOf(IdiomasTuki.KRIOL), IdiomasTuki.mencionados("¿Cómo se dice casa en inglés kriol?"))
        assertEquals(setOf(IdiomasTuki.INGLES), IdiomasTuki.mencionados("¿Cómo se dice casa en inglés?"))
        assertEquals(setOf(IdiomasTuki.MISKITO), IdiomasTuki.mencionados("háblame en miskitu"))
    }

    @Test fun detectaQueSePideOtroIdiomaDistintoAlQueAprende() {
        assertEquals(setOf(IdiomasTuki.INGLES), IdiomasTuki.otrosIdiomasPedidos("¿cómo se dice perro en inglés?", IdiomasTuki.MISKITO))
        assertTrue(IdiomasTuki.otrosIdiomasPedidos("¿cómo se dice perro en miskito?", IdiomasTuki.MISKITO).isEmpty())
        // Pedir la traducción al español no es cambiar de idioma.
        assertTrue(IdiomasTuki.otrosIdiomasPedidos("¿cómo se dice naksa en español?", IdiomasTuki.MISKITO).isEmpty())
    }

    @Test fun laRedireccionNombraElIdiomaActualYDondeCambiarlo() {
        val respuesta = IdiomasTuki.redirigirAOtroIdioma(IdiomasTuki.MISKITO, setOf(IdiomasTuki.KRIOL))
        assertTrue(respuesta.contains("estás aprendiendo Miskito"))
        assertTrue(respuesta.contains("Inglés Kriol"))
        assertTrue(respuesta.contains("pantalla principal"))
    }

    @Test fun clasificaPedidosDeFrasesEIdiomas() {
        assertEquals(TukiIntent.EXAMPLE, classifier.classify("Hazme una frase con agua"))
        assertEquals(TukiIntent.EXAMPLE, classifier.classify("dame una oración con la palabra casa"))
        assertEquals(TukiIntent.TRANSLATE, classifier.classify("¿Cómo digo buenos días?"))
        assertEquals(TukiIntent.AVAILABLE_LANGUAGES, classifier.classify("¿Qué idiomas puedo aprender?"))
        assertEquals(TukiIntent.AVAILABLE_LANGUAGES, classifier.classify("quiero cambiar de idioma"))
    }

    @Test fun reconoceConfusionQuejasYDudasSobreLaRespuesta() {
        // Frases reales de la prueba en el teléfono que antes iban al modelo y producían incoherencias.
        assertEquals(TukiIntent.CLARIFY, classifier.classify("no entiendo"))
        assertEquals(TukiIntent.COMPLAINT, classifier.classify("hablas sin sentido"))
        assertEquals(TukiIntent.COMPLAINT, classifier.classify("porque siempre me dices elije la opción que corresponde al contexto?"))
        assertEquals(TukiIntent.VERIFY_ANSWER, classifier.classify("entonces es falsa"))
    }
}
