package com.aikukisna.app.data.gramatica

import com.aikukisna.app.domain.gramatica.DecisionReglaGramatical
import com.aikukisna.app.domain.gramatica.MarcaGramatical
import com.aikukisna.app.domain.gramatica.MotorGramaticalControlado
import com.aikukisna.app.domain.gramatica.ReglaGramaticalValidada
import com.aikukisna.app.domain.gramatica.ReferenciaEvidenciaGramatical
import com.aikukisna.app.domain.gramatica.RepositorioEvidenciaGramatical
import com.aikukisna.app.domain.gramatica.EvidenciaGramaticalRegla
import com.aikukisna.app.domain.gramatica.ResultadoAnalisisGramatical
import com.aikukisna.app.domain.gramatica.ResultadoReglaGramatical
import com.aikukisna.app.domain.gramatica.SolicitudGeneracionGramatical
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotorGramaticalControladoImpl @Inject constructor(
    private val catalogo: CatalogoReglasGramaticales,
    private val evidenciaRepository: RepositorioEvidenciaGramatical
) : MotorGramaticalControlado {

    constructor(catalogo: CatalogoReglasGramaticales) : this(catalogo, RepositorioEvidenciaVacio)

    override suspend fun obtenerReglas(): List<ReglaGramaticalValidada> = catalogo.obtenerTodas()

    override suspend fun obtenerRegla(codigo: String): ReglaGramaticalValidada? =
        obtenerReglas().firstOrNull { it.codigo == codigo }

    override suspend fun analizar(codigo: String): ResultadoAnalisisGramatical? =
        obtenerRegla(codigo)?.let {
            ResultadoAnalisisGramatical(
                regla = it,
                permiteGeneracion = it.decision == DecisionReglaGramatical.GENERACION_CONTROLADA
            )
        }

    override suspend fun generar(solicitud: SolicitudGeneracionGramatical): ResultadoReglaGramatical {
        val regla = obtenerRegla(solicitud.codigoRegla)
            ?: return ResultadoReglaGramatical.NoAplicable("Regla no encontrada en el catálogo validado")
        when (regla.decision) {
            DecisionReglaGramatical.NO_EJECUTABLE ->
                return ResultadoReglaGramatical.Bloqueada("La regla está bloqueada para generación")
            DecisionReglaGramatical.SOLO_ANALISIS ->
                return ResultadoReglaGramatical.Bloqueada("La regla está autorizada únicamente para análisis")
            DecisionReglaGramatical.GENERACION_CONTROLADA -> Unit
        }
        if (solicitud.tieneExcepcionDocumentada) {
            return ResultadoReglaGramatical.NoAplicable("Existe una excepción documentada para esta entrada")
        }
        if (!solicitud.formaCompletaDocumentada.isNullOrBlank()) {
            return ResultadoReglaGramatical.NoAplicable(
                "Existe una forma completa documentada y debe conservarse con prioridad"
            )
        }

        val patron = regla.patronEjecutable
            ?: return ResultadoReglaGramatical.Bloqueada("La regla no contiene patrón ejecutable")
        val requeridas = marcasRequeridas[regla.codigo]
            ?: return ResultadoReglaGramatical.Bloqueada("La regla no tiene requisitos ejecutables registrados")
        val faltantes = requeridas - solicitud.marcas
        if (faltantes.isNotEmpty()) {
            return ResultadoReglaGramatical.NoAplicable(
                "Room no entregó las marcas requeridas: ${faltantes.joinToString { it.name }}"
            )
        }

        val placeholders = PLACEHOLDER.findAll(patron).map { it.groupValues[1] }.toSet()
        val componentesFaltantes = placeholders - solicitud.componentes.keys
        if (componentesFaltantes.isNotEmpty()) {
            return ResultadoReglaGramatical.NoAplicable(
                "Room no entregó los componentes requeridos: ${componentesFaltantes.joinToString()}"
            )
        }
        val componentesInvalidos = placeholders.mapNotNull { nombre ->
            val componente = solicitud.componentes.getValue(nombre)
            nombre.takeIf {
                componente.valor.isBlank() || !componente.documentado ||
                    componente.entidadRoomId <= 0L || componente.campoOrigen.isBlank()
            }
        }
        if (componentesInvalidos.isNotEmpty()) {
            return ResultadoReglaGramatical.NoAplicable(
                "Los componentes carecen de evidencia documentada en Room: ${componentesInvalidos.joinToString()}"
            )
        }

        val texto = construir(patron, solicitud)
            ?: return ResultadoReglaGramatical.Bloqueada("El patrón validado contiene una operación no permitida")
        return ResultadoReglaGramatical.Generada(
            texto = texto,
            regla = regla,
            entidadesRoomUsadas = placeholders.map {
                solicitud.componentes.getValue(it).entidadRoomId
            }.toSet()
        )
    }

    override suspend fun generarDesdeRoom(
        codigoRegla: String,
        referencia: ReferenciaEvidenciaGramatical
    ): ResultadoReglaGramatical {
        val regla = obtenerRegla(codigoRegla)
            ?: return ResultadoReglaGramatical.NoAplicable("Regla no encontrada en el catálogo validado")
        if (regla.decision != DecisionReglaGramatical.GENERACION_CONTROLADA) {
            return generar(SolicitudGeneracionGramatical(codigoRegla, emptyMap(), emptySet()))
        }
        val evidencia = evidenciaRepository.obtenerEvidencia(regla.id, referencia)
        val formaPrioritaria = evidencia.formas.firstOrNull { it.reglaId == regla.id }?.texto
            ?: evidencia.excepciones.firstNotNullOfOrNull { it.formaDocumentada?.texto }
        return generar(
            SolicitudGeneracionGramatical(
                codigoRegla = codigoRegla,
                componentes = evidencia.componentes,
                marcas = evidencia.marcas,
                formaCompletaDocumentada = formaPrioritaria,
                tieneExcepcionDocumentada = evidencia.excepciones.isNotEmpty()
            )
        )
    }

    private fun construir(patron: String, solicitud: SolicitudGeneracionGramatical): String? {
        val piezas = TOKEN.findAll(patron).toList()
        val residuo = TOKEN.replace(patron, "").replace("+", "").trim()
        if (residuo.isNotEmpty() || piezas.isEmpty()) return null
        return piezas.joinToString(separator = "") { match ->
            val token = match.value
            if (token.startsWith('<')) {
                solicitud.componentes.getValue(token.substring(1, token.length - 1)).valor
            } else {
                token.substring(1, token.length - 1)
            }
        }.trim()
    }

    private companion object {
        val PLACEHOLDER = Regex("<([A-Z_]+)>")
        val TOKEN = Regex("<[^>]+>|\"[^\"]*\"")

        val marcasRequeridas = mapOf(
            "infinitivo_aia" to setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.VERBO_REGULAR),
            "posposiciones" to setOf(MarcaGramatical.RELACION_SEMANTICA_RESUELTA),
            "negacion_ras" to setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.COMPATIBLE_NEGACION_RAS),
            "imperativo_s" to setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.VERBO_REGULAR),
            "imperativo_para" to setOf(MarcaGramatical.RAIZ_VERBAL, MarcaGramatical.VERBO_REGULAR),
            "articulo_indefinido_a" to setOf(MarcaGramatical.GRUPO_NOMINAL_INDEFINIDO),
            "articulo_definido_di" to setOf(MarcaGramatical.GRUPO_NOMINAL_DEFINIDO),
            "negacion_pasada_neva" to setOf(MarcaGramatical.ORACION_NEGATIVA, MarcaGramatical.REFERENCIA_PASADA),
            "modal_mos" to setOf(MarcaGramatical.MODALIDAD_OBLIGACION_NECESIDAD),
            "habitual_pasado_yuuztu" to setOf(MarcaGramatical.HABITUALIDAD_PASADA),
            "instrumental_wid" to setOf(MarcaGramatical.RELACION_INSTRUMENTAL)
        )
    }
}

private object RepositorioEvidenciaVacio : RepositorioEvidenciaGramatical {
    override suspend fun obtenerEvidencia(
        reglaId: Long,
        referencia: ReferenciaEvidenciaGramatical
    ) = EvidenciaGramaticalRegla(
        reglaId = reglaId,
        referencia = referencia,
        categorias = emptyList(),
        raices = emptyList(),
        formas = emptyList(),
        excepciones = emptyList(),
        marcas = emptySet(),
        componentes = emptyMap()
    )
}
