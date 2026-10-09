package com.aikukisna.app.data.local.ia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecorteRespuestaTest {

    @Test
    fun detectaBucleDeTokens() {
        assertTrue(RecorteRespuesta.debeCortar("Es como en el 7 7 7 7 7 7 7 7", 600))
    }

    @Test
    fun detectaOracionRepetida() {
        val texto = "Es como si estuviera en la Costa Caribe. Tiene un ritmo bonito. Es como si estuviera en la Costa Caribe."
        assertTrue(RecorteRespuesta.debeCortar(texto, 600))
    }

    @Test
    fun noCortaUnaRespuestaNormal() {
        assertFalse(RecorteRespuesta.debeCortar("¡Hola! Estás aprendiendo Miskito. ¿Practicamos una palabra?", 600))
    }

    @Test
    fun limpiaRepeticionesYColaDelBucle() {
        val texto = "¡Excelente! Vamos a practicar. Vamos a practicar. Es como 7 7 7 7 7 7 7"
        assertEquals("¡Excelente! Vamos a practicar.", RecorteRespuesta.limpiar(texto, 600))
    }

    @Test
    fun recortaEnElUltimoFinDeOracion() {
        val texto = "Primera oración completa aquí. Segunda oración que es bastante más larga y se corta"
        assertEquals("Primera oración completa aquí.", RecorteRespuesta.limpiar(texto, 50))
    }
}
