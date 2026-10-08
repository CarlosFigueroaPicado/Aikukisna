package com.aikukisna.app.domain.conocimiento

import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra

enum class EstadoValidacion(val value: String) {
    IMPORTADA("importada"), PENDIENTE_REVISION("pendiente_revision"), DOCUMENTADA("documentada"),
    VALIDADA("validada"), RECHAZADA("rechazada")
}

data class Acepcion(
    val id: Long,
    val palabraId: Int,
    val numero: Int,
    val definicion: String?,
    val contexto: String?,
    val categoriaGramatical: String?,
    val estado: EstadoValidacion
)

data class OpcionTraduccion(
    val palabra: Palabra,
    val acepcionOrigen: Acepcion? = null,
    val acepcionDestino: Acepcion? = null,
    val contexto: String? = null,
    val estado: EstadoValidacion = EstadoValidacion.IMPORTADA
)

sealed interface ResultadoBusquedaConocimiento {
    data class Exacta(val palabra: Palabra, val medianteVariante: String? = null) : ResultadoBusquedaConocimiento
    data class Sugerencias(val palabras: List<Palabra>) : ResultadoBusquedaConocimiento
    data object NoEncontrada : ResultadoBusquedaConocimiento
}

sealed interface ResolucionTraduccion {
    data class Expresion(val texto: String, val estado: EstadoValidacion) : ResolucionTraduccion
    data class Unica(val opcion: OpcionTraduccion) : ResolucionTraduccion
    data class Ambigua(val opciones: List<OpcionTraduccion>) : ResolucionTraduccion
    data class Sugerencia(val palabra: Palabra) : ResolucionTraduccion
    data class ComposicionDocumentada(val segmentos: List<SegmentoTraduccion>) : ResolucionTraduccion {
        val texto: String = segmentos.joinToString(" ") { it.textoDestino }
    }
    data class Parcial(val conocidas: List<String>, val desconocidas: List<String>) : ResolucionTraduccion
    data object NoEncontrada : ResolucionTraduccion
}

data class SegmentoTraduccion(
    val textoOrigen: String,
    val textoDestino: String,
    val estado: EstadoValidacion
)

data class EntradaConocimiento(
    val palabra: Palabra,
    val acepciones: List<Acepcion>,
    val traducciones: List<OpcionTraduccion>,
    val ejemplos: List<OracionEjemplo>
)

data class FraseVerificada(
    val texto: String,
    val traduccion: String,
    val idiomaTraduccionId: Int
)
