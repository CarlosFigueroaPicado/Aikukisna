package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.withTransaction
import com.aikukisna.app.data.local.dao.CategoriaDao
import com.aikukisna.app.data.local.dao.FuenteDocumentoDao
import com.aikukisna.app.data.local.dao.IdiomaDao
import com.aikukisna.app.data.local.dao.LeccionDao
import com.aikukisna.app.data.local.dao.LeccionPalabraDao
import com.aikukisna.app.data.local.dao.OracionEjemploDao
import com.aikukisna.app.data.local.dao.ContenidoLeccionDao
import com.aikukisna.app.data.local.dao.PalabraDao
import com.aikukisna.app.data.local.dao.TraduccionDao
import com.aikukisna.app.data.local.entity.CategoriaEntity
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.LeccionEntity
import com.aikukisna.app.data.local.entity.LeccionPalabraEntity
import com.aikukisna.app.data.local.entity.OracionEjemploEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.TraduccionEntity
import com.aikukisna.app.data.local.entity.MundoGamificadoEntity
import com.aikukisna.app.data.local.entity.LeccionExperienciaGamificadaEntity
import com.aikukisna.app.data.local.entity.ActividadLeccionEntity
import com.aikukisna.app.data.local.entity.ActividadRecursoEntity
import com.aikukisna.app.data.local.entity.EtapaRutaCurricularEntity
import com.aikukisna.app.data.local.entity.LeccionRutaCurricularEntity
import com.aikukisna.app.data.local.entity.EvidenciaCurricularLeccionEntity
import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.model.FuenteDocumento
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

@Serializable
private data class IdiomaSemilla(val id: Int, val codigo: String, val nombre: String)

@Serializable
private data class CategoriaSemilla(val id: Int, val nombre: String)

@Serializable
private data class FuenteSemilla(
    val id: Int,
    val titulo: String,
    val autor: String? = null,
    val anio: Int? = null,
    val institucion: String? = null
)

@Serializable
private data class PalabraSemilla(
    val id: Int,
    @SerialName("idioma_id") val idiomaId: Int,
    val texto: String,
    @SerialName("categoria_id") val categoriaId: Int? = null,
    @SerialName("fuente_id") val fuenteId: Int,
    val pronunciacion: String? = null,
    @SerialName("pronunciacion_fonetica") val pronunciacionFonetica: String? = null,
    @SerialName("pronunciacion_verificada") val pronunciacionVerificada: Boolean = false,
    @SerialName("texto_normalizado") val textoNormalizado: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada"
)

@Serializable
private data class TraduccionSemilla(
    val id: Int,
    @SerialName("palabra_origen_id") val palabraOrigenId: Int,
    @SerialName("palabra_destino_id") val palabraDestinoId: Int,
    val nota: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("fuente_id") val fuenteId: Int? = null,
    @SerialName("nivel_confianza") val nivelConfianza: Double? = null,
    @SerialName("es_preferida") val esPreferida: Boolean = false,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class CulturaSemilla(
    val id: Int,
    val titulo: String,
    val contenido: String,
    @SerialName("rango_pagina_inicio") val rangoPaginaInicio: Int? = null,
    @SerialName("rango_pagina_fin") val rangoPaginaFin: Int? = null,
    @SerialName("fuente_id") val fuenteId: Int
)

@Serializable
private data class LeccionSemilla(
    val id: Int,
    val titulo: String,
    @SerialName("capitulo_numero") val capituloNumero: Int? = null,
    val nivel: Int,
    @SerialName("categoria_id") val categoriaId: Int? = null,
    @SerialName("idioma_meta_id") val idiomaMetaId: Int
)

@Serializable
private data class LeccionPalabraSemilla(
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("palabra_id") val palabraId: Int
)

@Serializable
private data class OracionSemilla(
    val id: Int,
    @SerialName("texto_origen") val textoOrigen: String,
    @SerialName("texto_destino") val textoDestino: String,
    @SerialName("idioma_origen_id") val idiomaOrigenId: Int,
    @SerialName("idioma_destino_id") val idiomaDestinoId: Int,
    @SerialName("fuente_id") val fuenteId: Int,
    @SerialName("leccion_id") val leccionId: Int? = null,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class MundoGamificadoSemilla(
    val codigo: String,
    @SerialName("nombre_visible") val nombreVisible: String,
    @SerialName("descripcion_visible") val descripcionVisible: String,
    val orden: Int,
    @SerialName("icono_clave") val iconoClave: String? = null,
    @SerialName("xp_desbloqueo") val xpDesbloqueo: Int = 0,
    @SerialName("recompensa_final") val recompensaFinal: String? = null,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class ExperienciaGamificadaSemilla(
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("titulo_visible") val tituloVisible: String,
    @SerialName("subtitulo_visible") val subtituloVisible: String? = null,
    @SerialName("formato_principal") val formatoPrincipal: String,
    @SerialName("xp_base") val xpBase: Int,
    @SerialName("mundo_codigo") val mundoCodigo: String? = null,
    @SerialName("orden_en_mundo") val ordenEnMundo: Int? = null,
    @SerialName("recompensa_visible") val recompensaVisible: String? = null,
    @SerialName("mensaje_inicio_tuki") val mensajeInicioTuki: String? = null,
    @SerialName("mensaje_fin_tuki") val mensajeFinTuki: String? = null,
    @SerialName("usa_tuki") val usaTuki: Boolean = true,
    @SerialName("usa_audio") val usaAudio: Boolean = true,
    @SerialName("usa_lectura") val usaLectura: Boolean = true,
    @SerialName("usa_escritura") val usaEscritura: Boolean = true,
    @SerialName("usa_oralidad") val usaOralidad: Boolean = true,
    @SerialName("tiene_reto_final") val tieneRetoFinal: Boolean = true,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class ActividadLeccionSemilla(
    val id: Long,
    @SerialName("leccion_id") val leccionId: Int,
    val orden: Int,
    val codigo: String,
    val tipo: String,
    @SerialName("habilidad_curricular") val habilidadCurricular: String,
    @SerialName("mecanica_gamificada") val mecanicaGamificada: String,
    @SerialName("descripcion_estudiante") val descripcionEstudiante: String,
    @SerialName("evidencia_aprendizaje") val evidenciaAprendizaje: String? = null,
    val xp: Int,
    val obligatoria: Boolean = true,
    val activa: Boolean = true,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class ActividadRecursoSemilla(
    @SerialName("actividad_id") val actividadId: Long,
    @SerialName("tipo_recurso") val tipoRecurso: String,
    @SerialName("recurso_id") val recursoId: Long,
    val orden: Int,
    val rol: String,
    @SerialName("texto_principal") val textoPrincipal: String? = null,
    @SerialName("texto_apoyo") val textoApoyo: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String? = null,
    @SerialName("fuente_id") val fuenteId: Int? = null
)

@Serializable
private data class EtapaRutaCurricularSemilla(
    val codigo: String,
    val nombre: String,
    val orden: Int,
    val modalidad: String,
    @SerialName("grado_origen") val gradoOrigen: Int? = null,
    @SerialName("grado_destino") val gradoDestino: Int? = null,
    @SerialName("unidad_pedagogica_ciclo") val unidadPedagogicaCiclo: String? = null,
    val descripcion: String,
    val activa: Boolean = true
)

@Serializable
private data class LeccionRutaCurricularSemilla(
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("etapa_codigo") val etapaCodigo: String? = null,
    @SerialName("estado_mapeo") val estadoMapeo: String,
    @SerialName("es_refuerzo") val esRefuerzo: Boolean = false,
    @SerialName("es_transicion") val esTransicion: Boolean = false,
    @SerialName("prerrequisito_descripcion") val prerrequisitoDescripcion: String? = null,
    @SerialName("proposito_transicion") val propositoTransicion: String? = null,
    val justificacion: String? = null,
    @SerialName("fuente_primaria_id") val fuentePrimariaId: Int? = null,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class EvidenciaCurricularSemilla(
    val id: Long,
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("etapa_codigo") val etapaCodigo: String,
    @SerialName("fuente_id") val fuenteId: Int,
    val grado: Int,
    @SerialName("asignatura_area") val asignaturaArea: String,
    @SerialName("unidad_oficial") val unidadOficial: String? = null,
    @SerialName("competencia_eje_transversal") val competenciaEjeTransversal: String? = null,
    @SerialName("competencia_grado") val competenciaGrado: String? = null,
    @SerialName("indicador_logro") val indicadorLogro: String? = null,
    @SerialName("contenido_oficial") val contenidoOficial: String? = null,
    @SerialName("criterio_evaluacion") val criterioEvaluacion: String? = null,
    @SerialName("actividad_aikukisna") val actividadAikukisna: String? = null,
    @SerialName("evidencia_aprendizaje") val evidenciaAprendizaje: String? = null,
    @SerialName("pagina_seccion") val paginaSeccion: String? = null,
    @SerialName("tipo_correspondencia") val tipoCorrespondencia: String,
    @SerialName("aplica_sear") val aplicaSear: Boolean = false,
    @SerialName("eje_sear") val ejeSear: String? = null,
    @SerialName("adecuacion_intercultural") val adecuacionIntercultural: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String,
    val observacion: String? = null,
    @SerialName("lengua_aplicacion_id") val lenguaAplicacionId: Int? = null,
    @SerialName("naturaleza_aplicacion") val naturalezaAplicacion: String? = null,
    @SerialName("es_texto_oficial_literal") val esTextoOficialLiteral: Boolean = false,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = 0
)

@Serializable
private data class DiccionarioSemilla(
    val idiomas: List<IdiomaSemilla>,
    val categorias: List<CategoriaSemilla>,
    val fuentes: List<FuenteSemilla>,
    val palabras: List<PalabraSemilla>,
    val traducciones: List<TraduccionSemilla>,
    val culturas: List<CulturaSemilla>,
    val lecciones: List<LeccionSemilla>,
    @SerialName("leccion_palabras") val leccionPalabras: List<LeccionPalabraSemilla>,
    val oraciones: List<OracionSemilla>,
    val mundos: List<MundoGamificadoSemilla> = emptyList(),
    @SerialName("experiencias_gamificadas") val experienciasGamificadas: List<ExperienciaGamificadaSemilla> = emptyList(),
    val actividades: List<ActividadLeccionSemilla> = emptyList(),
    @SerialName("actividad_recursos") val actividadRecursos: List<ActividadRecursoSemilla> = emptyList(),
    @SerialName("etapas_curriculares") val etapasCurriculares: List<EtapaRutaCurricularSemilla> = emptyList(),
    @SerialName("rutas_curriculares") val rutasCurriculares: List<LeccionRutaCurricularSemilla> = emptyList(),
    @SerialName("evidencias_curriculares") val evidenciasCurriculares: List<EvidenciaCurricularSemilla> = emptyList()
)


@Serializable
private data class FilaExportada(val seed: DiccionarioSemilla)


class SembradorDiccionarioOffline @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AikukisnaDatabase,
    private val idiomaDao: IdiomaDao,
    private val categoriaDao: CategoriaDao,
    private val fuenteDocumentoDao: FuenteDocumentoDao,
    private val palabraDao: PalabraDao,
    private val traduccionDao: TraduccionDao,
    private val leccionDao: LeccionDao,
    private val leccionPalabraDao: LeccionPalabraDao,
    private val oracionEjemploDao: OracionEjemploDao,
    private val contenidoLeccionDao: ContenidoLeccionDao,
    private val culturaLocalCache: CulturaLocalCache
) {
    private val preferencias = context.getSharedPreferences("semilla_diccionario", Context.MODE_PRIVATE)

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun sembrarSiHaceFalta() {
        if (contenidoCompleto()) return

        val semilla = context.assets.open(ARCHIVO_SEMILLA).use { input ->
            JSON.decodeFromStream<FilaExportada>(input).seed
        }

        database.withTransaction {
            idiomaDao.guardarTodos(
                semilla.idiomas.map { IdiomaEntity(id = it.id, codigo = it.codigo, nombre = it.nombre) }
            )
            categoriaDao.guardarTodos(
                semilla.categorias.map { CategoriaEntity(id = it.id, nombre = it.nombre) }
            )
            fuenteDocumentoDao.guardarTodos(
                semilla.fuentes.map {
                    FuenteDocumentoEntity(
                        id = it.id,
                        titulo = it.titulo,
                        autor = it.autor,
                        anio = it.anio,
                        institucion = it.institucion
                    )
                }
            )
            palabraDao.guardarTodas(
                semilla.palabras.map {
                    PalabraEntity(
                        id = it.id,
                        idiomaId = it.idiomaId,
                        texto = it.texto,
                        categoriaId = it.categoriaId,
                        fuenteId = it.fuenteId,
                        pronunciacion = it.pronunciacion,
                        pronunciacionFonetica = it.pronunciacionFonetica,
                        pronunciacionVerificada = it.pronunciacionVerificada,
                        textoNormalizado = it.textoNormalizado?.takeIf { valor -> valor.isNotBlank() }
                            ?: NormalizadorLinguistico.normalizar(it.texto),
                        estadoValidacion = it.estadoValidacion
                    )
                }
            )
            traduccionDao.guardarTodas(
                semilla.traducciones.map {
                    TraduccionEntity(
                        id = it.id,
                        palabraOrigenId = it.palabraOrigenId,
                        palabraDestinoId = it.palabraDestinoId,
                        nota = it.nota,
                        estadoValidacion = it.estadoValidacion,
                        fuenteId = it.fuenteId,
                        nivelConfianza = it.nivelConfianza,
                        esPreferida = it.esPreferida,
                        updatedAtEpochMs = it.updatedAtEpochMs
                    )
                }
            )
            leccionDao.guardarTodas(
                semilla.lecciones.map {
                    LeccionEntity(
                        id = it.id,
                        titulo = it.titulo,
                        capituloNumero = it.capituloNumero,
                        nivel = it.nivel,
                        categoriaId = it.categoriaId,
                        idiomaMetaId = it.idiomaMetaId
                    )
                }
            )
            leccionPalabraDao.guardarTodas(
                semilla.leccionPalabras.map {
                    LeccionPalabraEntity(leccionId = it.leccionId, palabraId = it.palabraId)
                }
            )
            oracionEjemploDao.guardarTodas(
                semilla.oraciones.map {
                    OracionEjemploEntity(
                        id = it.id,
                        textoOrigen = it.textoOrigen,
                        textoDestino = it.textoDestino,
                        idiomaOrigenId = it.idiomaOrigenId,
                        idiomaDestinoId = it.idiomaDestinoId,
                        fuenteId = it.fuenteId,
                        leccionId = it.leccionId ?: 0,
                        estadoValidacion = it.estadoValidacion,
                        updatedAtEpochMs = it.updatedAtEpochMs
                    )
                }
            )
            contenidoLeccionDao.guardarMundos(semilla.mundos.map {
                MundoGamificadoEntity(it.codigo, it.nombreVisible, it.descripcionVisible, it.orden, it.iconoClave, it.xpDesbloqueo, it.recompensaFinal, it.updatedAtEpochMs)
            })
            contenidoLeccionDao.guardarExperiencias(semilla.experienciasGamificadas.map {
                LeccionExperienciaGamificadaEntity(it.leccionId, it.tituloVisible, it.subtituloVisible, it.formatoPrincipal, it.xpBase, it.mundoCodigo, it.ordenEnMundo, it.recompensaVisible, it.mensajeInicioTuki, it.mensajeFinTuki, it.usaTuki, it.usaAudio, it.usaLectura, it.usaEscritura, it.usaOralidad, it.tieneRetoFinal, it.updatedAtEpochMs)
            })
            contenidoLeccionDao.guardarActividades(semilla.actividades.map {
                ActividadLeccionEntity(it.id, it.leccionId, it.orden, it.codigo, it.tipo, it.habilidadCurricular, it.mecanicaGamificada, it.descripcionEstudiante, it.evidenciaAprendizaje, it.xp, it.obligatoria, it.activa, it.updatedAtEpochMs)
            })
            contenidoLeccionDao.guardarRecursos(semilla.actividadRecursos.map {
                ActividadRecursoEntity(it.actividadId, it.tipoRecurso, it.recursoId, it.orden, it.rol, it.textoPrincipal, it.textoApoyo, it.estadoValidacion, it.fuenteId)
            })
            contenidoLeccionDao.guardarEtapas(semilla.etapasCurriculares.map {
                EtapaRutaCurricularEntity(it.codigo, it.nombre, it.orden, it.modalidad, it.gradoOrigen, it.gradoDestino, it.unidadPedagogicaCiclo, it.descripcion, it.activa)
            })
            contenidoLeccionDao.guardarRutas(semilla.rutasCurriculares.map {
                LeccionRutaCurricularEntity(it.leccionId, it.etapaCodigo, it.estadoMapeo, it.esRefuerzo, it.esTransicion, it.prerrequisitoDescripcion, it.propositoTransicion, it.justificacion, it.fuentePrimariaId, it.updatedAtEpochMs)
            })
            contenidoLeccionDao.guardarEvidencias(semilla.evidenciasCurriculares.map {
                EvidenciaCurricularLeccionEntity(it.id, it.leccionId, it.etapaCodigo, it.fuenteId, it.grado, it.asignaturaArea, it.unidadOficial, it.competenciaEjeTransversal, it.competenciaGrado, it.indicadorLogro, it.contenidoOficial, it.criterioEvaluacion, it.actividadAikukisna, it.evidenciaAprendizaje, it.paginaSeccion, it.tipoCorrespondencia, it.aplicaSear, it.ejeSear, it.adecuacionIntercultural, it.estadoValidacion, it.observacion, it.lenguaAplicacionId, it.naturalezaAplicacion, it.esTextoOficialLiteral, it.updatedAtEpochMs)
            })
        }

        val fuentesPorId = semilla.fuentes.associateBy { it.id }
        culturaLocalCache.guardar(
            semilla.culturas.mapNotNull { c ->
                val fuente = fuentesPorId[c.fuenteId] ?: return@mapNotNull null
                CulturaContenido(
                    id = c.id,
                    titulo = c.titulo,
                    contenido = c.contenido,
                    rangoPaginaInicio = c.rangoPaginaInicio,
                    rangoPaginaFin = c.rangoPaginaFin,
                    fuente = FuenteDocumento(
                        id = fuente.id,
                        titulo = fuente.titulo,
                        autor = fuente.autor,
                        anio = fuente.anio,
                        institucion = fuente.institucion
                    )
                )
            }
        )


        preferencias.edit().putInt(CLAVE_VERSION, VERSION_SEMILLA).apply()
    }

    suspend fun hayContenidoDisponible(): Boolean {
        if (idiomaDao.contarTodos() == 0) return false
        if (palabraDao.contarTodas() == 0) return false
        if (traduccionDao.contarTodas() == 0) return false
        return leccionDao.contarTodas() > 0
    }

    private suspend fun contenidoCompleto(): Boolean {
        if (preferencias.getInt(CLAVE_VERSION, 0) < VERSION_SEMILLA) return false
        if (idiomaDao.contarTodos() < CONTEO_IDIOMAS) return false
        if (traduccionDao.contarTodas() < CONTEO_TRADUCCIONES) return false
        if (leccionDao.contarTodas() < CONTEO_LECCIONES) return false
        if (leccionPalabraDao.contarTodos() < CONTEO_LECCION_PALABRAS) return false
        if (oracionEjemploDao.contarTodas() < CONTEO_ORACIONES) return false
        if (culturaLocalCache.leer().size < CONTEO_CULTURAS) return false
        return CONTEO_PALABRAS_POR_IDIOMA.all { (idiomaId, minimo) ->
            palabraDao.contarPorIdioma(idiomaId) >= minimo
        }
    }

    private companion object {
        const val ARCHIVO_SEMILLA = "diccionario_semilla.json"
        const val CLAVE_VERSION = "version"
        const val VERSION_SEMILLA = 2
        const val CONTEO_IDIOMAS = 4
        const val CONTEO_TRADUCCIONES = 56_885
        const val CONTEO_CULTURAS = 54
        const val CONTEO_LECCIONES = 76
        const val CONTEO_LECCION_PALABRAS = 465
        const val CONTEO_ORACIONES = 499
        val CONTEO_PALABRAS_POR_IDIOMA = mapOf(1 to 33_136, 2 to 30_487, 3 to 35, 4 to 50)
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
