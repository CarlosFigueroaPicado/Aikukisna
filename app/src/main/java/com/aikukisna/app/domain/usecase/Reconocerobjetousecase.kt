package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject


private const val IDIOMA_ESPANOL = 2

private const val PROMPT_RECONOCIMIENTO =
    "¿Qué objeto es este? Respondé con una sola palabra en español, sin " +
            "artículos (\"el\", \"la\") ni explicaciones adicionales."


class ReconocerObjetoUseCase @Inject constructor(
    private val iaRepository: IaRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(
        imagenBase64: String,
        idiomaMetaId: Int,
        mimeType: String = "image/jpeg"
    ): ResultadoReconocimiento {
        require(imagenBase64.isNotBlank()) { "La imagen no puede estar vacía" }

        val objetoDetectado = iaRepository
            .preguntarConImagen(PROMPT_RECONOCIMIENTO, imagenBase64, mimeType)
            .trim()
            .trim('.', '"', '\'')

        val traduccion = buscarEnDiccionario(objetoDetectado, idiomaMetaId)

        return ResultadoReconocimiento(
            objetoDetectado = objetoDetectado,
            traduccion = traduccion
        )
    }

    private suspend fun buscarEnDiccionario(objeto: String, idiomaMetaId: Int): String? {
        val candidatos = diccionarioRepository.buscarPalabras(
            query = objeto,
            idiomaId = IDIOMA_ESPANOL,
            limite = 5,
            offset = 0
        )
        val coincidenciaExacta = candidatos.firstOrNull { it.texto.equals(objeto, ignoreCase = true) }
            ?: return null

        return diccionarioRepository.obtenerTraducciones(coincidenciaExacta.id)
            .firstOrNull { it.palabraDestino.idioma.id == idiomaMetaId }
            ?.palabraDestino
            ?.texto
    }
}