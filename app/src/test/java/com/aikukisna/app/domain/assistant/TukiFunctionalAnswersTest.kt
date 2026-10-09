package com.aikukisna.app.domain.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TukiFunctionalAnswersTest {
    @Test fun respondeIdiomaDesdeElContextoReal() {
        listOf("Miskito", "Inglés Kriol", "Inglés Estándar").forEach { idioma ->
            assertTrue(functionalAnswer(TukiIntent.SELECTED_LANGUAGE, idioma).contains(idioma))
        }
    }

    @Test fun sinPerfilNoInventaIdioma() {
        val respuesta = functionalAnswer(TukiIntent.SELECTED_LANGUAGE, null)
        assertTrue(respuesta.contains("No encuentro un perfil local"))
        assertFalse(respuesta.contains("Miskito"))
    }

    @Test fun explicaUtilidadDeXpSinInventarSaldo() {
        val respuesta = functionalAnswer(TukiIntent.XP_HELP, null)
        assertTrue(respuesta.contains("500 XP"))
        assertTrue(respuesta.contains("85%"))
        assertFalse(respuesta.contains("tienes"))
    }
}
