package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.aPalabraDomain
import com.aikukisna.app.data.local.aPalabraEntity
import com.aikukisna.app.data.remote.dto.PalabraDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PalabraPronunciacionMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun dtoAceptaPronunciacionNula() {
        val dto = json.decodeFromString<PalabraDto>(jsonPalabra())

        assertNull(dto.pronunciacion)
        assertNull(dto.pronunciacionFonetica)
        assertFalse(dto.pronunciacionVerificada)
    }

    @Test
    fun dtoRecibeFoneticaYVerificacion() {
        val dto = json.decodeFromString<PalabraDto>(
            jsonPalabra(
                extra = ""","pronunciacion":"Nák-sa","pronunciacion_fonetica":"nak.sa","pronunciacion_verificada":true"""
            )
        )

        assertEquals("Nák-sa", dto.pronunciacion)
        assertEquals("nak.sa", dto.pronunciacionFonetica)
        assertTrue(dto.pronunciacionVerificada)
    }

    @Test
    fun dtoDominioRoomYDominioConservanMetadatos() {
        val inicial = json.decodeFromString<PalabraDto>(
            jsonPalabra(
                extra = ""","pronunciacion":"Nák-sa","pronunciacion_fonetica":"nak.sa","pronunciacion_verificada":true"""
            )
        ).toDomain()

        val entity = inicial.aPalabraEntity()
        val recuperada = entity.aPalabraDomain(inicial.idioma, inicial.categoria, inicial.fuente)

        assertEquals("Nák-sa", inicial.pronunciacion)
        assertEquals("nak.sa", inicial.pronunciacionFonetica)
        assertTrue(inicial.pronunciacionVerificada)
        assertEquals(inicial.pronunciacion, entity.pronunciacion)
        assertEquals(inicial.pronunciacionFonetica, entity.pronunciacionFonetica)
        assertEquals(inicial.pronunciacionVerificada, entity.pronunciacionVerificada)
        assertEquals(inicial.pronunciacion, recuperada.pronunciacion)
        assertEquals(inicial.pronunciacionFonetica, recuperada.pronunciacionFonetica)
        assertEquals(inicial.pronunciacionVerificada, recuperada.pronunciacionVerificada)
    }

    private fun jsonPalabra(extra: String = "") = """
        {
          "id": 1,
          "idioma": {"id": 1, "codigo": "mi", "nombre": "Miskito"},
          "texto": "Naksa",
          "categoria": null,
          "fuente_documento": {"id": 1, "titulo": "Fuente", "autor": null, "anio": null, "institucion": null}
          $extra
        }
    """.trimIndent()
}
