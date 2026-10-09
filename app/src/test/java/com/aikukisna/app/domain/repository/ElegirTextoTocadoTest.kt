package com.aikukisna.app.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ElegirTextoTocadoTest {

    private val lineas = listOf(
        LineaTexto("Bienvenidos", RegionObjeto(0.1f, 0.10f, 0.9f, 0.18f), bloque = 0),
        LineaTexto("SALIDA DE", RegionObjeto(0.2f, 0.50f, 0.8f, 0.56f), bloque = 1),
        LineaTexto("EMERGENCIA", RegionObjeto(0.2f, 0.57f, 0.8f, 0.63f), bloque = 1),
        LineaTexto("Horario de atención de lunes a viernes", RegionObjeto(0.1f, 0.80f, 0.9f, 0.84f), bloque = 2)
    )

    @Test
    fun tomaSoloElTextoTocado() {
        assertEquals("Bienvenidos", elegirTextoTocado(lineas, 0.5f, 0.14f)?.texto)
        assertEquals("Horario de atención de lunes a viernes", elegirTextoTocado(lineas, 0.3f, 0.82f)?.texto)
    }

    @Test
    fun unCartelCortoSeLeeCompleto() {
        val elegido = elegirTextoTocado(lineas, 0.5f, 0.52f)
        assertEquals("SALIDA DE EMERGENCIA", elegido?.texto)
        assertEquals(0.63f, elegido?.region?.abajo)
    }

    @Test
    fun unToqueLejosDelTextoNoEligeNada() {
        assertNull(elegirTextoTocado(lineas, 0.5f, 0.35f))
    }
}
