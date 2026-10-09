package com.aikukisna.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PartesAlineadasTest {

    @Test
    fun separaSaludoYPreguntaDeUnaOracionRegistrada() {
        val pares = partesAlineadas("¡Hola, Pedro! ¿Cómo estás?", "¡Naksa, Pedro! ¿Nahki sma?")
        assertTrue(("¿Cómo estás?" to "¿Nahki sma?") in pares)
        assertTrue(("¡Hola!" to "¡Naksa!") in pares)
    }

    @Test
    fun noAlineaSiLasPartesNoCoinciden() {
        assertEquals(emptyList<Pair<String, String>>(), partesAlineadas("Hola. ¿Cómo estás?", "Naksa nahki sma"))
    }
}
