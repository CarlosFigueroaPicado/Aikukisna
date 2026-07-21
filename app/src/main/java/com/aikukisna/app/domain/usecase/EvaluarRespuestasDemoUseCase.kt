package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra

data class ResultadoDemo(
    val correctas: Int,
    val total: Int,
    val puntosGanados: Int
)

class EvaluarRespuestasDemoUseCase {
    operator fun invoke(respuestas: Map<Palabra, String>): ResultadoDemo {
        val correctas = respuestas.count { (palabra, respuestaUsuario) ->
            respuestaUsuario.trim().equals(palabra.texto.trim(), ignoreCase = true)
        }
        return ResultadoDemo(
            correctas = correctas,
            total = respuestas.size,
            puntosGanados = correctas * 10
        )
    }
}