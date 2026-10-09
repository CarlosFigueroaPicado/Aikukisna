package com.aikukisna.app.domain.conocimiento

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizadorLinguisticoTest {
    @Test fun normalizaUnicodeMayusculasYEspaciosSinPerderDiacriticos() {
        val composed = "  ÁMIGO   YÂ  "
        assertEquals("ámigo yâ", NormalizadorLinguistico.normalizar(composed))
    }

    @Test fun eliminaPuntuacionPerifericaSinAlterarLaInterna() {
        assertEquals("hola", NormalizadorLinguistico.normalizar("  ¡HOLA!  "))
        assertEquals("don't", NormalizadorLinguistico.normalizar("Don't"))
        assertEquals("a-b", NormalizadorLinguistico.normalizar("A-B"))
    }
}
