package com.aikukisna.app.domain.repository

/**
 * Preferencias de aprendizaje del estudiante guardadas en el teléfono.
 *
 * La lengua de apoyo es la que se usa para explicar las lecciones de Español: quien aprende
 * español en la Costa Caribe suele tener el Miskito o el Kriol como lengua materna.
 */
interface PreferenciasAprendizaje {
    fun lenguaApoyoId(): Int
    fun cambiarLenguaApoyo(idiomaId: Int)
}
