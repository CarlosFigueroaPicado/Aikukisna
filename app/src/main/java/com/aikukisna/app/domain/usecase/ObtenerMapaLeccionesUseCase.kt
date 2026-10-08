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

data class MapaLeccionesResultado(
    val lecciones: List<LeccionConEstado>,
    val nivelesDesbloqueados: Set<Int>
)

class ObtenerMapaLeccionesUseCase @Inject constructor(
    private val obtenerLeccionesUseCase: ObtenerLeccionesUseCase,
    private val obtenerProgresoUseCase: ObtenerProgresoUseCase
) {
    suspend operator fun invoke(usuarioId: UUID, idiomaMetaId: Int, nivel: Int): MapaLeccionesResultado {
        val todasLasLecciones = obtenerLeccionesUseCase()
            .filter { it.idiomaMeta.id == idiomaMetaId }
            // Los diálogos sin capítulo repasan lo visto: van al final de su nivel, no antes de "Saludos".
            .sortedWith(compareBy({ it.nivel }, { it.capituloNumero ?: Int.MAX_VALUE }, { it.id }))

        val progresoPorLeccionId = obtenerProgresoUseCase(usuarioId)
            .associateBy { it.leccion.id }

        fun estaAprobada(leccion: Leccion): Boolean {
            val progreso = progresoPorLeccionId[leccion.id]
            return progreso?.estado == "completada" && (progreso.puntaje ?: 0) >= PORCENTAJE_APROBACION
        }

        val nivelesOrdenados = todasLasLecciones.map { it.nivel }.distinct().sorted()
        val nivelesDesbloqueados = buildSet {
            nivelesOrdenados.forEachIndexed { indice, nivelActual ->
                val nivelesPrevios = nivelesOrdenados.take(indice).toSet()
                if (indice == 0 || todasLasLecciones.filter { it.nivel in nivelesPrevios }.all(::estaAprobada)) {
                    add(nivelActual)
                }
            }
        }

        val leccionesDelNivel = todasLasLecciones.filter { it.nivel == nivel }
        val primeraPendienteGlobal = todasLasLecciones.firstOrNull { !estaAprobada(it) }?.id

        val resultado = leccionesDelNivel.map { leccion ->
            val progreso = progresoPorLeccionId[leccion.id]
            val estado = when {
                estaAprobada(leccion) -> EstadoLeccion.COMPLETADA
                leccion.id == primeraPendienteGlobal -> EstadoLeccion.ACTUAL
                else -> EstadoLeccion.BLOQUEADA
            }
            LeccionConEstado(leccion = leccion, estado = estado, puntaje = progreso?.puntaje)
        }
        return MapaLeccionesResultado(resultado, nivelesDesbloqueados)
    }

    companion object {
        const val PORCENTAJE_APROBACION = 85
    }
}
