package com.aikukisna.app.presentacion.componentes

import java.time.LocalTime

/**
 * Saludos y felicitaciones de la interfaz en el idioma que se aprende, tomados del material
 * documentado. Donde no hay un equivalente documentado se usa el español.
 */
object FrasesIdioma {
    private const val MISKITO = 1
    private const val KRIOL = 3
    private const val INGLES = 4

    fun saludo(idiomaId: Int?, hora: LocalTime = LocalTime.now()): String = when (idiomaId) {
        MISKITO -> "¡Naksa!"
        INGLES -> "Hello!"
        // En Kriol solo están documentados los saludos según la hora (lecciones SEAR).
        KRIOL -> when {
            hora.hour < 12 -> "Maanin!"
            hora.hour < 18 -> "Gud dekyah!"
            else -> "Gud nait!"
        }
        else -> "¡Hola!"
    }

    fun felicitacion(idiomaId: Int?): String = when (idiomaId) {
        MISKITO -> "¡Pain pali! (Excelente)"
        INGLES -> "Great! (Excelente)"
        else -> "¡Excelente!"
    }
}
