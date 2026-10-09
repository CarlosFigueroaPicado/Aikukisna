package com.aikukisna.app.domain.gramatica

enum class DecisionReglaGramatical(val valor: String) {
    GENERACION_CONTROLADA("generacion_controlada"),
    SOLO_ANALISIS("solo_analisis"),
    NO_EJECUTABLE("no_ejecutable")
}

data class EjemploReglaValidada(
    val textoIdioma: String,
    val traduccionEspanol: String?,
    val estadoValidacion: String,
    val fuenteId: Int?
)

data class ReglaGramaticalValidada(
    val id: Long,
    val idiomaId: Int,
    val codigo: String,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val patronDocumentado: String?,
    val aplicacionDocumentada: String?,
    val estadoOriginal: String,
    val ejemplos: List<EjemploReglaValidada>,
    val decision: DecisionReglaGramatical,
    val patronEjecutable: String?,
    val restricciones: String,
    val observacionesValidador: String
)

enum class MarcaGramatical {
    RAIZ_VERBAL,
    VERBO_REGULAR,
    COMPATIBLE_NEGACION_RAS,
    RELACION_SEMANTICA_RESUELTA,
    GRUPO_NOMINAL_INDEFINIDO,
    GRUPO_NOMINAL_DEFINIDO,
    ORACION_NEGATIVA,
    REFERENCIA_PASADA,
    MODALIDAD_OBLIGACION_NECESIDAD,
    HABITUALIDAD_PASADA,
    RELACION_INSTRUMENTAL
}

data class ComponenteGramaticalDocumentado(
    val valor: String,
    val entidadRoomId: Long,
    val campoOrigen: String,
    val documentado: Boolean
)

data class SolicitudGeneracionGramatical(
    val codigoRegla: String,
    val componentes: Map<String, ComponenteGramaticalDocumentado>,
    val marcas: Set<MarcaGramatical>,
    val formaCompletaDocumentada: String? = null,
    val tieneExcepcionDocumentada: Boolean = false
)

data class ReferenciaEvidenciaGramatical(
    val tipoEntidad: String,
    val entidadId: Long
)

data class CategoriaGramaticalDocumentada(
    val codigo: String,
    val nombre: String,
    val estadoValidacion: String
)

data class RaizVerbalDocumentada(
    val id: Long,
    val acepcionId: Long?,
    val texto: String,
    val regularidad: String?,
    val estadoValidacion: String
)

data class FormaGramaticalDocumentada(
    val id: Long,
    val raizId: Long?,
    val codigoForma: String,
    val texto: String,
    val reglaId: Long?,
    val estadoValidacion: String
)

data class ExcepcionGramaticalDocumentada(
    val reglaId: Long,
    val referencia: ReferenciaEvidenciaGramatical,
    val motivo: String,
    val formaDocumentada: FormaGramaticalDocumentada?
)

data class EvidenciaGramaticalRegla(
    val reglaId: Long,
    val referencia: ReferenciaEvidenciaGramatical,
    val categorias: List<CategoriaGramaticalDocumentada>,
    val raices: List<RaizVerbalDocumentada>,
    val formas: List<FormaGramaticalDocumentada>,
    val excepciones: List<ExcepcionGramaticalDocumentada>,
    val marcas: Set<MarcaGramatical>,
    val componentes: Map<String, ComponenteGramaticalDocumentado>
)

sealed interface ResultadoReglaGramatical {
    data class Generada(
        val texto: String,
        val regla: ReglaGramaticalValidada,
        val entidadesRoomUsadas: Set<Long>
    ) : ResultadoReglaGramatical

    data class NoAplicable(val motivo: String) : ResultadoReglaGramatical
    data class Bloqueada(val motivo: String) : ResultadoReglaGramatical
}

data class ResultadoAnalisisGramatical(
    val regla: ReglaGramaticalValidada,
    val permiteGeneracion: Boolean
)
