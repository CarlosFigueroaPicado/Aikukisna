package com.aikukisna.app.presentacion.pantallas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionAlrededorDelToqueTest {
    @Test
    fun centraElAreaEnElDedo() {
        val region = regionAlrededorDelToque(0.5f, 0.5f)
        assertTrue(region.contiene(0.5f, 0.5f))
        assertEquals(0.5f, (region.izquierda + region.derecha) / 2, 0.001f)
    }

    @Test
    fun noSeSaleDeLaImagenEnLosBordes() {
        val region = regionAlrededorDelToque(0.02f, 0.98f)
        assertTrue(region.izquierda >= 0f && region.abajo <= 1f)
        assertTrue(region.contiene(0.02f, 0.98f))
    }
}
