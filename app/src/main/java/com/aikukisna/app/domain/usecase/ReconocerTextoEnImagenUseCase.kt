package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.domain.repository.ReconocedorTextoLocal
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ReconocerTextoEnImagenUseCase @Inject constructor(
    private val traducirTextoUseCase: TraducirTextoUseCase,
    private val reconocedorTextoLocal: ReconocedorTextoLocal,
    private val traducirOracionUseCase: TraducirOracionUseCase? = null
) {
    suspend operator fun invoke(
        imagenBase64: String,
        idiomaDestinoId: Int
    ): ResultadoReconocimiento {
        require(imagenBase64.isNotBlank()) {
            "La imagen no puede estar vacía"
        }

        try {
            val textoReconocido = reconocedorTextoLocal.reconocer(imagenBase64).trim()

            if (textoReconocido.isBlank()) {
                error("No se encontró texto en la imagen")
            }

            val traduccion = traducirTextoLocal(
                texto = textoReconocido,
                idiomaDestinoId = idiomaDestinoId
            )

            return ResultadoReconocimiento(
                objetoDetectado = textoReconocido,
                traduccion = traduccion
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(
                e.message ?: "No se pudo leer y traducir el texto de la imagen",
                e
            )
        }
    }

    private suspend fun traducirTextoLocal(
        texto: String,
        idiomaDestinoId: Int
    ): String {
        val idiomasPosibles = listOf(2, 4, 3, 1)
            .filterNot { it == idiomaDestinoId }

        idiomasPosibles.forEach { idiomaOrigenId ->
            try {
                return traducirTextoUseCase(
                    texto,
                    idiomaOrigenId,
                    idiomaDestinoId
                ).texto
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Probar el siguiente idioma disponible.
            }
        }

        // Carteles y frases rara vez están completos en el corpus: se intenta la traducción
        // de oraciones sin conexión, indicando que no es una traducción verificada.
        traducirOracionUseCase?.let { traducirOracion ->
            idiomasPosibles.forEach { idiomaOrigenId ->
                try {
                    val resultado = traducirOracion(texto, idiomaOrigenId, idiomaDestinoId)
                    val etiqueta = when (resultado.tipo) {
                        TipoTraduccion.VERIFICADA -> return resultado.texto
                        TipoTraduccion.LITERAL -> "traducción literal"
                        TipoTraduccion.AUTOMATICA -> "traducción automática"
                    }
                    return "${resultado.texto} ($etiqueta)"
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Probar el siguiente idioma disponible.
                }
            }
        }

        error("No hay una traducción disponible para el texto reconocido")
    }
}
