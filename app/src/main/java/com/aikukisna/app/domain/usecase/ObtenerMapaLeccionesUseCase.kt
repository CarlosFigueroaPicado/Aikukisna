package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Leccion
import java.util.UUID
import javax.inject.Inject

enum class EstadoLeccion { COMPLETADA, ACTUAL, BLOQUEADA }

data class LeccionConEstado(
    val leccion: Leccion,
    val estado: EstadoLeccion,
    val puntaje: Int?
)

class ObtenerMapaLeccionesUseCase @Inject constructor(
    private val obtenerLeccionesUseCase: ObtenerLeccionesUseCase,
    private val obtenerProgresoUseCase: ObtenerProgresoUseCase
) {
    suspend operator fun invoke(usuarioId: UUID, idiomaMetaId: Int, nivel: Int): List<LeccionConEstado> {
        val leccionesDelNivel = obtenerLeccionesUseCase()
            .filter { it.idiomaMeta.id == idiomaMetaId && it.nivel == nivel }
            .sortedWith(compareBy({ it.capituloNumero ?: 0 }, { it.id }))

        val progresoPorLeccionId = obtenerProgresoUseCase(usuarioId)
            .associateBy { it.leccion.id }

        var yaHayActual = false
        return leccionesDelNivel.map { leccion ->
            val progreso = progresoPorLeccionId[leccion.id]
            val estado = when {
                progreso?.estado == "completada" -> EstadoLeccion.COMPLETADA
                !yaHayActual -> {
                    yaHayActual = true
                    EstadoLeccion.ACTUAL
                }
                else -> EstadoLeccion.BLOQUEADA
            }
            LeccionConEstado(leccion = leccion, estado = estado, puntaje = progreso?.puntaje)
        }
    }
}