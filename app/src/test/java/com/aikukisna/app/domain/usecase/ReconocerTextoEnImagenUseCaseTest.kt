package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.ReconocedorTextoLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReconocerTextoEnImagenUseCaseTest {
    private val miskito = Idioma(1, "mi", "Miskito")
    private val espanol = Idioma(2, "es", "Español")
    private val fuente = FuenteDocumento(1, "Diccionario verificado", null, null, null)
    private val agua = Palabra(2088, espanol, "agua", null, fuente)
    private val li = Palabra(2089, miskito, "lî", null, fuente)
    private val repositorio = RepositorioDiccionarioImagen(
        palabras = listOf(agua, li),
        traducciones = listOf(Traduccion(1643, agua, li, null))
    )

    @Test
    fun reconoceTextoConMlKitYLoTraduceConElDiccionario() {
        val casoDeUso = ReconocerTextoEnImagenUseCase(
            traducirTextoUseCase = TraducirTextoUseCase(repositorio),
            reconocedorTextoLocal = ReconocedorTextoLocal { "  agua  " }
        )

        val resultado = ejecutarReconocimiento { casoDeUso("imagen", miskito.id) }

        assertEquals("agua", resultado.objetoDetectado)
        assertEquals("lî", resultado.traduccion)
    }

    @Test
    fun rechazaUnaImagenSinTexto() {
        val casoDeUso = ReconocerTextoEnImagenUseCase(
            traducirTextoUseCase = TraducirTextoUseCase(repositorio),
            reconocedorTextoLocal = ReconocedorTextoLocal { "   " }
        )

        val error = assertThrows(IllegalStateException::class.java) {
            ejecutarReconocimiento { casoDeUso("imagen", miskito.id) }
        }

        assertEquals("No se encontró texto en la imagen", error.message)
    }
}
