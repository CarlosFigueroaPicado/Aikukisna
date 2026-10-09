package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.domain.repository.RegionObjeto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeleccionRegionObjetoTest {

    @Test
    fun `elige la caja de menor area cuando dos cajas contienen el toque`() {
        val grande = RegionObjeto(0.1f, 0.1f, 0.9f, 0.9f)
        val pequena = RegionObjeto(0.4f, 0.4f, 0.6f, 0.6f)

        assertEquals(
            pequena,
            seleccionarRegionPorToque(listOf(grande, pequena), 0.5f, 0.5f)
        )
    }

    @Test
    fun `no selecciona por proximidad cuando el toque cae fuera de todas las cajas`() {
        val region = RegionObjeto(0.1f, 0.1f, 0.3f, 0.3f)

        assertNull(seleccionarRegionPorToque(listOf(region), 0.31f, 0.31f))
    }
}
