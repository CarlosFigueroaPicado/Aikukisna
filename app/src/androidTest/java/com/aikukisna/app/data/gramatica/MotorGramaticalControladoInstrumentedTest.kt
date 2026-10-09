package com.aikukisna.app.data.gramatica

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aikukisna.app.domain.gramatica.ComponenteGramaticalDocumentado
import com.aikukisna.app.domain.gramatica.DecisionReglaGramatical
import com.aikukisna.app.domain.gramatica.MarcaGramatical
import com.aikukisna.app.domain.gramatica.ResultadoReglaGramatical
import com.aikukisna.app.domain.gramatica.SolicitudGeneracionGramatical
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MotorGramaticalControladoInstrumentedTest {
    private val catalogo = CatalogoReglasGramaticales(
        InstrumentationRegistry.getInstrumentation().targetContext
    )
    private val motor = MotorGramaticalControladoImpl(catalogo)

    @Test
    fun cargaLasCuarentaReglasConLasDecisionesEsperadas() = runBlocking {
        val reglas = motor.obtenerReglas()

        assertEquals(40, reglas.size)
        assertEquals(11, reglas.count { it.decision == DecisionReglaGramatical.GENERACION_CONTROLADA })
        assertEquals(16, reglas.count { it.decision == DecisionReglaGramatical.SOLO_ANALISIS })
        assertEquals(13, reglas.count { it.decision == DecisionReglaGramatical.NO_EJECUTABLE })
    }

    @Test
    fun bloqueaUnaReglaDeSoloAnalisis() = runBlocking {
        val resultado = motor.generar(
            SolicitudGeneracionGramatical("orden_basico_sov", emptyMap(), emptySet())
        )

        assertTrue(resultado is ResultadoReglaGramatical.Bloqueada)
    }

    @Test
    fun noAplicaSiFaltaEvidenciaDeRoom() = runBlocking {
        val resultado = motor.generar(
            SolicitudGeneracionGramatical(
                codigoRegla = "infinitivo_aia",
                componentes = mapOf(
                    "RAIZ_VERBAL_DOCUMENTADA" to ComponenteGramaticalDocumentado(
                        valor = "kaik",
                        entidadRoomId = 0,
                        campoOrigen = "",
                        documentado = false
                    )
                ),
                marcas = setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.VERBO_REGULAR)
            )
        )

        assertTrue(resultado is ResultadoReglaGramatical.NoAplicable)
    }

    @Test
    fun conservaLaFormaCompletaDocumentada() = runBlocking {
        val resultado = motor.generar(
            SolicitudGeneracionGramatical(
                codigoRegla = "infinitivo_aia",
                componentes = emptyMap(),
                marcas = emptySet(),
                formaCompletaDocumentada = "kaikaia"
            )
        )

        assertTrue(resultado is ResultadoReglaGramatical.NoAplicable)
    }

    @Test
    fun generaSoloConTodosLosRequisitosDocumentados() = runBlocking {
        val resultado = motor.generar(
            SolicitudGeneracionGramatical(
                codigoRegla = "infinitivo_aia",
                componentes = mapOf(
                    "RAIZ_VERBAL_DOCUMENTADA" to ComponenteGramaticalDocumentado(
                        valor = "kaik",
                        entidadRoomId = 31,
                        campoOrigen = "raiz_verbal",
                        documentado = true
                    )
                ),
                marcas = setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.VERBO_REGULAR)
            )
        )

        assertTrue(resultado is ResultadoReglaGramatical.Generada)
        resultado as ResultadoReglaGramatical.Generada
        assertEquals("kaikaia", resultado.texto)
        assertEquals(setOf(31L), resultado.entidadesRoomUsadas)
    }
}
