package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra

sealed interface ContenidoLeccion {
    data class Vocabulario(val palabras: List<Palabra>) : ContenidoLeccion
    data class Frases(val oraciones: List<OracionEjemplo>) : ContenidoLeccion
}

interface LeccionRepository {
    suspend fun obtenerLecciones(nivel: Int? = null): List<Leccion>
    suspend fun obtenerLeccionPorId(id: Int): Leccion?
    suspend fun obtenerContenidoLeccion(leccionId: Int): ContenidoLeccion
}