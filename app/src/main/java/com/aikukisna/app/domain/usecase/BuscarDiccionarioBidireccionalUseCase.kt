package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import javax.inject.Inject

sealed interface ResultadoDiccionarioBidireccional {
    data class Exactas(val entradas: List<EntradaConocimiento>) : ResultadoDiccionarioBidireccional
    data class Sugerencias(val palabras: List<Palabra>) : ResultadoDiccionarioBidireccional
    data object NoEncontrada : ResultadoDiccionarioBidireccional
}

class BuscarDiccionarioBidireccionalUseCase @Inject constructor(
    private val conocimiento: RepositorioConocimiento
) {
    suspend operator fun invoke(
        texto: String,
        idiomaBase: Idioma,
        idiomaAprendizaje: Idioma
    ): ResultadoDiccionarioBidireccional {
        if (texto.isBlank()) return ResultadoDiccionarioBidireccional.NoEncontrada
        val direcciones = listOf(
            idiomaBase to idiomaAprendizaje,
            idiomaAprendizaje to idiomaBase
        ).distinctBy { it.first.id to it.second.id }

        val exactas = mutableListOf<EntradaConocimiento>()
        val sugerencias = mutableListOf<Palabra>()
        for ((origen, destino) in direcciones) {
            when (val resultado = conocimiento.buscarPalabra(texto, origen.id)) {
                is ResultadoBusquedaConocimiento.Exacta -> {
                    conocimiento.obtenerEntrada(resultado.palabra.id, destino.id)?.let(exactas::add)
                }
                is ResultadoBusquedaConocimiento.Sugerencias -> sugerencias += resultado.palabras
                ResultadoBusquedaConocimiento.NoEncontrada -> Unit
            }
        }
        if (exactas.isNotEmpty()) {
            return ResultadoDiccionarioBidireccional.Exactas(exactas.distinctBy { it.palabra.id })
        }
        val unicas = sugerencias.distinctBy { it.id }
        return if (unicas.isEmpty()) ResultadoDiccionarioBidireccional.NoEncontrada
        else ResultadoDiccionarioBidireccional.Sugerencias(unicas)
    }
}
