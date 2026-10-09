package com.aikukisna.app.data.gramatica

import com.aikukisna.app.data.local.dao.EvidenciaGramaticalDao
import com.aikukisna.app.data.local.entity.FormaVerbalDocumentadaEntity
import com.aikukisna.app.data.local.entity.RaizVerbalEntity
import com.aikukisna.app.domain.gramatica.CategoriaGramaticalDocumentada
import com.aikukisna.app.domain.gramatica.ComponenteGramaticalDocumentado
import com.aikukisna.app.domain.gramatica.EvidenciaGramaticalRegla
import com.aikukisna.app.domain.gramatica.ExcepcionGramaticalDocumentada
import com.aikukisna.app.domain.gramatica.FormaGramaticalDocumentada
import com.aikukisna.app.domain.gramatica.MarcaGramatical
import com.aikukisna.app.domain.gramatica.RaizVerbalDocumentada
import com.aikukisna.app.domain.gramatica.ReferenciaEvidenciaGramatical
import com.aikukisna.app.domain.gramatica.RepositorioEvidenciaGramatical
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositorioEvidenciaGramaticalRoom @Inject constructor(
    private val dao: EvidenciaGramaticalDao
) : RepositorioEvidenciaGramatical {

    override suspend fun obtenerEvidencia(
        reglaId: Long,
        referencia: ReferenciaEvidenciaGramatical
    ): EvidenciaGramaticalRegla {
        val categorias = if (referencia.tipoEntidad == TIPO_ACEPCION) {
            dao.obtenerCategoriasCompletasDeAcepcion(referencia.entidadId)
                .filter { esConfiable(it.estadoValidacion) }
                .map { CategoriaGramaticalDocumentada(it.codigo, it.nombre, it.estadoValidacion) }
        } else emptyList()

        val entidadesRaiz = when (referencia.tipoEntidad) {
            TIPO_ACEPCION -> dao.obtenerRaicesDeAcepcion(referencia.entidadId)
            TIPO_RAIZ_VERBAL -> listOfNotNull(dao.obtenerRaiz(referencia.entidadId))
            else -> emptyList()
        }.filter { esConfiable(it.estadoValidacion) }
        val formas = entidadesRaiz.flatMap { raiz ->
            dao.obtenerFormasDeRaiz(raiz.id).filter { esConfiable(it.estadoValidacion) }
        }

        val excepciones = dao.obtenerExcepciones(
            reglaId,
            referencia.tipoEntidad,
            referencia.entidadId
        ).filter { esConfiable(it.estadoValidacion) }.map { excepcion ->
            ExcepcionGramaticalDocumentada(
                reglaId = excepcion.reglaId,
                referencia = referencia,
                motivo = excepcion.motivo,
                formaDocumentada = excepcion.formaDocumentadaId
                    ?.let { dao.obtenerForma(it) }
                    ?.takeIf { esConfiable(it.estadoValidacion) }
                    ?.toDomain()
            )
        }

        val marcasGuardadas = dao.obtenerMarcas(referencia.tipoEntidad, referencia.entidadId)
            .asSequence()
            .filter { it.reglaId == reglaId && esConfiable(it.estadoValidacion) }
            .mapNotNull { runCatching { MarcaGramatical.valueOf(it.codigoMarca) }.getOrNull() }
            .toMutableSet()
        entidadesRaiz.forEach { raiz ->
            marcasGuardadas += MarcaGramatical.RAIZ_VERBAL
            if (raiz.regularidad.equals(REGULAR, ignoreCase = true)) {
                marcasGuardadas += MarcaGramatical.VERBO_REGULAR
            }
        }

        val componentes = dao.obtenerComponentes(
            reglaId,
            referencia.tipoEntidad,
            referencia.entidadId
        ).filter { esConfiable(it.estadoValidacion) }.associate { componente ->
            componente.nombreComponente to ComponenteGramaticalDocumentado(
                valor = componente.valorDocumentado,
                entidadRoomId = componente.entidadOrigenId,
                campoOrigen = componente.campoOrigen,
                documentado = true
            )
        }

        return EvidenciaGramaticalRegla(
            reglaId = reglaId,
            referencia = referencia,
            categorias = categorias,
            raices = entidadesRaiz.map { it.toDomain() },
            formas = formas.map { it.toDomain() },
            excepciones = excepciones,
            marcas = marcasGuardadas,
            componentes = componentes
        )
    }

    private fun RaizVerbalEntity.toDomain() = RaizVerbalDocumentada(
        id = id,
        acepcionId = acepcionId,
        texto = texto,
        regularidad = regularidad,
        estadoValidacion = estadoValidacion
    )

    private fun FormaVerbalDocumentadaEntity.toDomain() = FormaGramaticalDocumentada(
        id = id,
        raizId = raizId,
        codigoForma = codigoForma,
        texto = texto,
        reglaId = reglaId,
        estadoValidacion = estadoValidacion
    )

    private fun esConfiable(estado: String): Boolean =
        estado == ESTADO_DOCUMENTADA || estado == ESTADO_VALIDADA

    private companion object {
        const val TIPO_ACEPCION = "acepcion"
        const val TIPO_RAIZ_VERBAL = "raiz_verbal"
        const val REGULAR = "regular"
        const val ESTADO_DOCUMENTADA = "documentada"
        const val ESTADO_VALIDADA = "validada"
    }
}
