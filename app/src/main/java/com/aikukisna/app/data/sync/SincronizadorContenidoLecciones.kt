package com.aikukisna.app.data.sync

import androidx.room.withTransaction
import com.aikukisna.app.data.local.AikukisnaDatabase
import com.aikukisna.app.data.local.dao.ContenidoLeccionDao
import com.aikukisna.app.data.local.entity.ActividadLeccionEntity
import com.aikukisna.app.data.local.entity.ActividadRecursoEntity
import com.aikukisna.app.data.local.entity.EtapaRutaCurricularEntity
import com.aikukisna.app.data.local.entity.EvidenciaCurricularLeccionEntity
import com.aikukisna.app.data.local.entity.LeccionExperienciaGamificadaEntity
import com.aikukisna.app.data.local.entity.LeccionRutaCurricularEntity
import com.aikukisna.app.data.local.entity.MundoGamificadoEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SincronizadorContenidoLecciones @Inject constructor(
    private val client: SupabaseClient,
    private val database: AikukisnaDatabase,
    private val dao: ContenidoLeccionDao
) {
    suspend fun sincronizar(): Int {
        val mundos = client.from("mundo_gamificado")
            .select { order("orden", Order.ASCENDING) }
            .decodeList<MundoDto>()

        val experiencias = client.from("leccion_experiencia_gamificada")
            .select { order("leccion_id", Order.ASCENDING) }
            .decodeList<ExperienciaDto>()

        val actividades = client.from("actividad_leccion")
            .select {
                filter { eq("activa", true) }
                order("leccion_id", Order.ASCENDING)
                order("orden", Order.ASCENDING)
            }
            .decodeList<ActividadDto>()

        val recursos = client.from("vista_actividad_recurso_resuelto")
            .select {
                order("actividad_id", Order.ASCENDING)
                order("orden", Order.ASCENDING)
            }
            .decodeList<RecursoDto>()

        val etapas = client.from("etapa_ruta_curricular")
            .select { order("orden", Order.ASCENDING) }
            .decodeList<EtapaDto>()

        val rutas = client.from("leccion_ruta_curricular")
            .select { order("leccion_id", Order.ASCENDING) }
            .decodeList<RutaDto>()

        val evidencias = client.from("evidencia_curricular_leccion")
            .select {
                order("leccion_id", Order.ASCENDING)
                order("id", Order.ASCENDING)
            }
            .decodeList<EvidenciaDto>()
            .filter { it.estadoValidacion != "rechazada" }

        database.withTransaction {
            dao.borrarRecursos()
            dao.borrarActividades()
            dao.borrarEvidencias()
            dao.borrarRutas()
            dao.borrarEtapas()
            dao.borrarExperiencias()
            dao.borrarMundos()

            dao.guardarMundos(mundos.map(MundoDto::toEntity))
            dao.guardarExperiencias(experiencias.map(ExperienciaDto::toEntity))
            dao.guardarActividades(actividades.map(ActividadDto::toEntity))
            dao.guardarRecursos(recursos.map(RecursoDto::toEntity))
            dao.guardarEtapas(etapas.map(EtapaDto::toEntity))
            dao.guardarRutas(rutas.map(RutaDto::toEntity))
            dao.guardarEvidencias(evidencias.map(EvidenciaDto::toEntity))
        }

        return mundos.size + experiencias.size + actividades.size + recursos.size +
            etapas.size + rutas.size + evidencias.size
    }
}

@Serializable
private data class MundoDto(
    val codigo: String,
    @SerialName("nombre_visible") val nombreVisible: String,
    @SerialName("descripcion_visible") val descripcionVisible: String,
    val orden: Int,
    @SerialName("icono_clave") val iconoClave: String? = null,
    @SerialName("xp_desbloqueo") val xpDesbloqueo: Int = 0,
    @SerialName("recompensa_final") val recompensaFinal: String? = null,
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = MundoGamificadoEntity(
        codigo, nombreVisible, descripcionVisible, orden, iconoClave,
        xpDesbloqueo, recompensaFinal, epoch(updatedAt)
    )
}

@Serializable
private data class ExperienciaDto(
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
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = LeccionExperienciaGamificadaEntity(
        leccionId, tituloVisible, subtituloVisible, formatoPrincipal, xpBase,
        mundoCodigo, ordenEnMundo, recompensaVisible, mensajeInicioTuki,
        mensajeFinTuki, usaTuki, usaAudio, usaLectura, usaEscritura,
        usaOralidad, tieneRetoFinal, epoch(updatedAt)
    )
}

@Serializable
private data class ActividadDto(
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
    val obligatoria: Boolean,
    val activa: Boolean,
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = ActividadLeccionEntity(
        id, leccionId, orden, codigo, tipo, habilidadCurricular,
        mecanicaGamificada, descripcionEstudiante, evidenciaAprendizaje,
        xp, obligatoria, activa, epoch(updatedAt)
    )
}

@Serializable
private data class RecursoDto(
    @SerialName("actividad_id") val actividadId: Long,
    @SerialName("tipo_recurso") val tipoRecurso: String,
    @SerialName("recurso_id") val recursoId: Long,
    val orden: Int,
    val rol: String,
    @SerialName("texto_principal") val textoPrincipal: String? = null,
    @SerialName("texto_apoyo") val textoApoyo: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String? = null,
    @SerialName("fuente_id") val fuenteId: Int? = null
) {
    fun toEntity() = ActividadRecursoEntity(
        actividadId, tipoRecurso, recursoId, orden, rol,
        textoPrincipal, textoApoyo, estadoValidacion, fuenteId
    )
}

@Serializable
private data class EtapaDto(
    val codigo: String,
    val nombre: String,
    val orden: Int,
    val modalidad: String,
    @SerialName("grado_origen") val gradoOrigen: Int? = null,
    @SerialName("grado_destino") val gradoDestino: Int? = null,
    @SerialName("unidad_pedagogica_ciclo") val unidadPedagogicaCiclo: String? = null,
    val descripcion: String,
    val activa: Boolean
) {
    fun toEntity() = EtapaRutaCurricularEntity(
        codigo, nombre, orden, modalidad, gradoOrigen, gradoDestino,
        unidadPedagogicaCiclo, descripcion, activa
    )
}

@Serializable
private data class RutaDto(
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("etapa_codigo") val etapaCodigo: String? = null,
    @SerialName("estado_mapeo") val estadoMapeo: String,
    @SerialName("es_refuerzo") val esRefuerzo: Boolean,
    @SerialName("es_transicion") val esTransicion: Boolean,
    @SerialName("prerrequisito_descripcion") val prerrequisitoDescripcion: String? = null,
    @SerialName("proposito_transicion") val propositoTransicion: String? = null,
    val justificacion: String? = null,
    @SerialName("fuente_primaria_id") val fuentePrimariaId: Int? = null,
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = LeccionRutaCurricularEntity(
        leccionId, etapaCodigo, estadoMapeo, esRefuerzo, esTransicion,
        prerrequisitoDescripcion, propositoTransicion, justificacion,
        fuentePrimariaId, epoch(updatedAt)
    )
}

@Serializable
private data class EvidenciaDto(
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
    @SerialName("aplica_sear") val aplicaSear: Boolean,
    @SerialName("eje_sear") val ejeSear: String? = null,
    @SerialName("adecuacion_intercultural") val adecuacionIntercultural: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String,
    val observacion: String? = null,
    @SerialName("lengua_aplicacion_id") val lenguaAplicacionId: Int? = null,
    @SerialName("naturaleza_aplicacion") val naturalezaAplicacion: String? = null,
    @SerialName("es_texto_oficial_literal") val esTextoOficialLiteral: Boolean,
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = EvidenciaCurricularLeccionEntity(
        id, leccionId, etapaCodigo, fuenteId, grado, asignaturaArea,
        unidadOficial, competenciaEjeTransversal, competenciaGrado,
        indicadorLogro, contenidoOficial, criterioEvaluacion,
        actividadAikukisna, evidenciaAprendizaje, paginaSeccion,
        tipoCorrespondencia, aplicaSear, ejeSear, adecuacionIntercultural,
        estadoValidacion, observacion, lenguaAplicacionId,
        naturalezaAplicacion, esTextoOficialLiteral, epoch(updatedAt)
    )
}

private fun epoch(value: String): Long = Instant.parse(value).toEpochMilli()
