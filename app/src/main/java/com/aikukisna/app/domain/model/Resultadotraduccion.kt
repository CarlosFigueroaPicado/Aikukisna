package com.aikukisna.app.domain.model


enum class FuenteTraduccion { DICCIONARIO, IA }

/**
 * VERIFICADA: frase, expresión o palabra documentada en el corpus.
 * LITERAL: composición palabra por palabra con equivalencias del diccionario (orden no validado).
 * AUTOMATICA: generada por el modelo local; útil para entender, no está validada.
 */
enum class TipoTraduccion { VERIFICADA, LITERAL, AUTOMATICA }

data class ResultadoTraduccion(
    val texto: String,
    val fuente: FuenteTraduccion,
    val alternativas: List<String> = emptyList(),
    val contexto: String? = null,
    val tipo: TipoTraduccion = TipoTraduccion.VERIFICADA,
    val nota: String? = null
)
