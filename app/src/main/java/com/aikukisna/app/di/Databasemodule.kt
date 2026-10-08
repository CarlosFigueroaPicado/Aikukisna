package com.aikukisna.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aikukisna.app.data.local.AikukisnaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE oracion_ejemplo_cache ADD COLUMN idiomaOrigenId INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE oracion_ejemplo_cache ADD COLUMN idiomaDestinoId INTEGER NOT NULL DEFAULT 2")
            db.execSQL("UPDATE oracion_ejemplo_cache SET idiomaOrigenId = 2, idiomaDestinoId = 3 WHERE id BETWEEN 415 AND 439")
            db.execSQL("UPDATE oracion_ejemplo_cache SET idiomaOrigenId = 3, idiomaDestinoId = 2 WHERE id BETWEEN 440 AND 464")
            db.execSQL("UPDATE oracion_ejemplo_cache SET idiomaOrigenId = 4, idiomaDestinoId = 2 WHERE id BETWEEN 465 AND 499")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN pronunciacion TEXT")
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN pronunciacion_fonetica TEXT")
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN pronunciacion_verificada INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS audio_pronunciacion_cache (
                    palabraId INTEGER NOT NULL,
                    idiomaCodigo TEXT NOT NULL,
                    tipoReferencia TEXT NOT NULL,
                    referencia TEXT NOT NULL,
                    origen TEXT NOT NULL,
                    verificado INTEGER NOT NULL,
                    PRIMARY KEY(palabraId, idiomaCodigo, tipoReferencia, referencia),
                    FOREIGN KEY(palabraId) REFERENCES palabra_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_audio_pronunciacion_cache_palabraId_idiomaCodigo_verificado_origen
                ON audio_pronunciacion_cache(palabraId, idiomaCodigo, verificado, origen)
            """.trimIndent())
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS memoria_tuki_local (
                    id TEXT NOT NULL PRIMARY KEY,
                    usuarioId TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    resumen TEXT NOT NULL,
                    palabrasClave TEXT NOT NULL,
                    fechaEpochMs INTEGER NOT NULL,
                    usos INTEGER NOT NULL DEFAULT 0,
                    sincronizada INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_memoria_tuki_local_usuarioId_fechaEpochMs ON memoria_tuki_local(usuarioId, fechaEpochMs)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_memoria_tuki_local_sincronizada ON memoria_tuki_local(sincronizada)")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE memoria_tuki_local ADD COLUMN tema TEXT")
            db.execSQL("ALTER TABLE memoria_tuki_local ADD COLUMN ultimaPalabraOFrase TEXT")
            db.execSQL("ALTER TABLE memoria_tuki_local ADD COLUMN ultimaLeccionId INTEGER")
            db.execSQL("ALTER TABLE memoria_tuki_local ADD COLUMN ultimaIntencion TEXT")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN texto_normalizado TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN estado_validacion TEXT NOT NULL DEFAULT 'importada'")
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN created_at_epoch_ms INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE palabra_cache ADD COLUMN updated_at_epoch_ms INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE palabra_cache SET texto_normalizado = lower(trim(texto)) WHERE texto_normalizado = ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_palabra_cache_idiomaId_texto_normalizado ON palabra_cache(idiomaId, texto_normalizado)")
            db.execSQL("CREATE TABLE IF NOT EXISTS palabra_fuente_cache (palabraId INTEGER NOT NULL, fuenteId INTEGER NOT NULL, paginaInicio INTEGER, paginaFin INTEGER, nota TEXT, updatedAtEpochMs INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(palabraId, fuenteId))")
            db.execSQL("INSERT OR IGNORE INTO palabra_fuente_cache(palabraId, fuenteId) SELECT id, fuenteId FROM palabra_cache")
            db.execSQL("CREATE TABLE IF NOT EXISTS acepcion_cache (id INTEGER NOT NULL PRIMARY KEY, palabraId INTEGER NOT NULL, numeroAcepcion INTEGER NOT NULL, definicion TEXT, contexto TEXT, categoriaGramatical TEXT, estadoValidacion TEXT NOT NULL, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_acepcion_cache_palabraId_numeroAcepcion ON acepcion_cache(palabraId, numeroAcepcion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_acepcion_cache_updatedAtEpochMs ON acepcion_cache(updatedAtEpochMs)")
            db.execSQL("CREATE TABLE IF NOT EXISTS traduccion_acepcion_cache (id INTEGER NOT NULL PRIMARY KEY, acepcionOrigenId INTEGER NOT NULL, acepcionDestinoId INTEGER NOT NULL, tipo TEXT NOT NULL, estadoValidacion TEXT NOT NULL, nivelConfianza REAL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_acepcion_cache_acepcionOrigenId ON traduccion_acepcion_cache(acepcionOrigenId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_acepcion_cache_acepcionDestinoId ON traduccion_acepcion_cache(acepcionDestinoId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_acepcion_cache_updatedAtEpochMs ON traduccion_acepcion_cache(updatedAtEpochMs)")
            db.execSQL("CREATE TABLE IF NOT EXISTS variante_palabra_cache (id INTEGER NOT NULL PRIMARY KEY, palabraId INTEGER NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, tipo TEXT NOT NULL, estadoValidacion TEXT NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_variante_palabra_cache_textoNormalizado_palabraId ON variante_palabra_cache(textoNormalizado, palabraId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_variante_palabra_cache_updatedAtEpochMs ON variante_palabra_cache(updatedAtEpochMs)")
            db.execSQL("CREATE TABLE IF NOT EXISTS expresion_cache (id INTEGER NOT NULL PRIMARY KEY, idiomaId INTEGER NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, tipo TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_expresion_cache_idiomaId_textoNormalizado ON expresion_cache(idiomaId, textoNormalizado)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_expresion_cache_updatedAtEpochMs ON expresion_cache(updatedAtEpochMs)")
            db.execSQL("CREATE TABLE IF NOT EXISTS traduccion_expresion_cache (id INTEGER NOT NULL PRIMARY KEY, expresionOrigenId INTEGER NOT NULL, expresionDestinoId INTEGER NOT NULL, estadoValidacion TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_expresion_cache_expresionOrigenId ON traduccion_expresion_cache(expresionOrigenId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_expresion_cache_expresionDestinoId ON traduccion_expresion_cache(expresionDestinoId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_traduccion_expresion_cache_updatedAtEpochMs ON traduccion_expresion_cache(updatedAtEpochMs)")
            db.execSQL("CREATE TABLE IF NOT EXISTS sincronizacion_linguistica (recurso TEXT NOT NULL PRIMARY KEY, ultimoUpdatedAtEpochMs INTEGER NOT NULL, ultimoId INTEGER NOT NULL)")
        }
    }


    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("UPDATE palabra_cache SET texto_normalizado = lower(trim(texto)) WHERE texto_normalizado = ''")
            db.execSQL("ALTER TABLE traduccion_cache ADD COLUMN estadoValidacion TEXT NOT NULL DEFAULT 'importada'")
            db.execSQL("ALTER TABLE traduccion_cache ADD COLUMN fuenteId INTEGER")
            db.execSQL("ALTER TABLE traduccion_cache ADD COLUMN nivelConfianza REAL")
            db.execSQL("ALTER TABLE traduccion_cache ADD COLUMN esPreferida INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE traduccion_cache ADD COLUMN updatedAtEpochMs INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE oracion_ejemplo_cache ADD COLUMN estadoValidacion TEXT NOT NULL DEFAULT 'importada'")
            db.execSQL("ALTER TABLE oracion_ejemplo_cache ADD COLUMN updatedAtEpochMs INTEGER NOT NULL DEFAULT 0")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS mundo_gamificado_cache (
                    codigo TEXT NOT NULL PRIMARY KEY,
                    nombreVisible TEXT NOT NULL,
                    descripcionVisible TEXT NOT NULL,
                    orden INTEGER NOT NULL,
                    iconoClave TEXT,
                    xpDesbloqueo INTEGER NOT NULL,
                    recompensaFinal TEXT,
                    updatedAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS leccion_experiencia_gamificada_cache (
                    leccionId INTEGER NOT NULL PRIMARY KEY,
                    tituloVisible TEXT NOT NULL,
                    subtituloVisible TEXT,
                    formatoPrincipal TEXT NOT NULL,
                    xpBase INTEGER NOT NULL,
                    mundoCodigo TEXT,
                    ordenEnMundo INTEGER,
                    recompensaVisible TEXT,
                    mensajeInicioTuki TEXT,
                    mensajeFinTuki TEXT,
                    usaTuki INTEGER NOT NULL,
                    usaAudio INTEGER NOT NULL,
                    usaLectura INTEGER NOT NULL,
                    usaEscritura INTEGER NOT NULL,
                    usaOralidad INTEGER NOT NULL,
                    tieneRetoFinal INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_leccion_experiencia_gamificada_cache_mundoCodigo ON leccion_experiencia_gamificada_cache(mundoCodigo)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS actividad_leccion_cache (
                    id INTEGER NOT NULL PRIMARY KEY,
                    leccionId INTEGER NOT NULL,
                    orden INTEGER NOT NULL,
                    codigo TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    habilidadCurricular TEXT NOT NULL,
                    mecanicaGamificada TEXT NOT NULL,
                    descripcionEstudiante TEXT NOT NULL,
                    evidenciaAprendizaje TEXT,
                    xp INTEGER NOT NULL,
                    obligatoria INTEGER NOT NULL,
                    activa INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_actividad_leccion_cache_leccionId_orden ON actividad_leccion_cache(leccionId, orden)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_actividad_leccion_cache_leccionId_codigo ON actividad_leccion_cache(leccionId, codigo)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS actividad_recurso_cache (
                    actividadId INTEGER NOT NULL,
                    tipoRecurso TEXT NOT NULL,
                    recursoId INTEGER NOT NULL,
                    orden INTEGER NOT NULL,
                    rol TEXT NOT NULL,
                    textoPrincipal TEXT,
                    textoApoyo TEXT,
                    estadoValidacion TEXT,
                    fuenteId INTEGER,
                    PRIMARY KEY(actividadId, tipoRecurso, recursoId)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_actividad_recurso_cache_actividadId_orden ON actividad_recurso_cache(actividadId, orden)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS etapa_ruta_curricular_cache (
                    codigo TEXT NOT NULL PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    orden INTEGER NOT NULL,
                    modalidad TEXT NOT NULL,
                    gradoOrigen INTEGER,
                    gradoDestino INTEGER,
                    unidadPedagogicaCiclo TEXT,
                    descripcion TEXT NOT NULL,
                    activa INTEGER NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS leccion_ruta_curricular_cache (
                    leccionId INTEGER NOT NULL PRIMARY KEY,
                    etapaCodigo TEXT,
                    estadoMapeo TEXT NOT NULL,
                    esRefuerzo INTEGER NOT NULL,
                    esTransicion INTEGER NOT NULL,
                    prerrequisitoDescripcion TEXT,
                    propositoTransicion TEXT,
                    justificacion TEXT,
                    fuentePrimariaId INTEGER,
                    updatedAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_leccion_ruta_curricular_cache_etapaCodigo ON leccion_ruta_curricular_cache(etapaCodigo)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS evidencia_curricular_leccion_cache (
                    id INTEGER NOT NULL PRIMARY KEY,
                    leccionId INTEGER NOT NULL,
                    etapaCodigo TEXT NOT NULL,
                    fuenteId INTEGER NOT NULL,
                    grado INTEGER NOT NULL,
                    asignaturaArea TEXT NOT NULL,
                    unidadOficial TEXT,
                    competenciaEjeTransversal TEXT,
                    competenciaGrado TEXT,
                    indicadorLogro TEXT,
                    contenidoOficial TEXT,
                    criterioEvaluacion TEXT,
                    actividadAikukisna TEXT,
                    evidenciaAprendizaje TEXT,
                    paginaSeccion TEXT,
                    tipoCorrespondencia TEXT NOT NULL,
                    aplicaSear INTEGER NOT NULL,
                    ejeSear TEXT,
                    adecuacionIntercultural TEXT,
                    estadoValidacion TEXT NOT NULL,
                    observacion TEXT,
                    lenguaAplicacionId INTEGER,
                    naturalezaAplicacion TEXT,
                    esTextoOficialLiteral INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_evidencia_curricular_leccion_cache_leccionId ON evidencia_curricular_leccion_cache(leccionId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_evidencia_curricular_leccion_cache_etapaCodigo ON evidencia_curricular_leccion_cache(etapaCodigo)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_evidencia_curricular_leccion_cache_fuenteId ON evidencia_curricular_leccion_cache(fuenteId)")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val statements = listOf(
                "CREATE TABLE IF NOT EXISTS cultura_contenido_cache (id INTEGER NOT NULL PRIMARY KEY, titulo TEXT NOT NULL, contenido TEXT NOT NULL, rangoPaginaInicio INTEGER, rangoPaginaFin INTEGER, fuenteId INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE TABLE IF NOT EXISTS regla_gramatical_cache (id INTEGER NOT NULL PRIMARY KEY, idiomaId INTEGER NOT NULL, codigo TEXT NOT NULL, categoria TEXT NOT NULL, titulo TEXT NOT NULL, descripcion TEXT NOT NULL, patron TEXT, aplicacion TEXT, productiva INTEGER NOT NULL, prioridad INTEGER NOT NULL, fuenteId INTEGER, estadoValidacion TEXT NOT NULL, notas TEXT, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE INDEX IF NOT EXISTS index_regla_gramatical_cache_idiomaId ON regla_gramatical_cache(idiomaId)",
                "CREATE INDEX IF NOT EXISTS index_regla_gramatical_cache_fuenteId ON regla_gramatical_cache(fuenteId)",
                "CREATE INDEX IF NOT EXISTS index_regla_gramatical_cache_updatedAtEpochMs ON regla_gramatical_cache(updatedAtEpochMs)",
                "CREATE TABLE IF NOT EXISTS ejemplo_regla_gramatical_cache (id INTEGER NOT NULL PRIMARY KEY, reglaId INTEGER NOT NULL, textoIdioma TEXT NOT NULL, traduccionEspanol TEXT, fuenteId INTEGER, estadoValidacion TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, idiomaTraduccionId INTEGER)",
                "CREATE INDEX IF NOT EXISTS index_ejemplo_regla_gramatical_cache_reglaId ON ejemplo_regla_gramatical_cache(reglaId)",
                "CREATE INDEX IF NOT EXISTS index_ejemplo_regla_gramatical_cache_fuenteId ON ejemplo_regla_gramatical_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS regla_pronunciacion_cache (id INTEGER NOT NULL PRIMARY KEY, idiomaId INTEGER NOT NULL, patron TEXT NOT NULL, tipo TEXT NOT NULL, descripcion TEXT NOT NULL, reemplazoFonetico TEXT, ejemplo TEXT, fuenteId INTEGER, prioridad INTEGER NOT NULL, activa INTEGER NOT NULL, createdAtEpochMs INTEGER NOT NULL, estadoValidacion TEXT NOT NULL, notas TEXT, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE INDEX IF NOT EXISTS index_regla_pronunciacion_cache_idiomaId ON regla_pronunciacion_cache(idiomaId)",
                "CREATE INDEX IF NOT EXISTS index_regla_pronunciacion_cache_fuenteId ON regla_pronunciacion_cache(fuenteId)",
                "CREATE INDEX IF NOT EXISTS index_regla_pronunciacion_cache_updatedAtEpochMs ON regla_pronunciacion_cache(updatedAtEpochMs)",
                "CREATE TABLE IF NOT EXISTS leccion_expresion_cache (leccionId INTEGER NOT NULL, expresionId INTEGER NOT NULL, tipoVinculo TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(leccionId, expresionId, tipoVinculo))",
                "CREATE TABLE IF NOT EXISTS leccion_oracion_cache (leccionId INTEGER NOT NULL, oracionId INTEGER NOT NULL, tipoVinculo TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(leccionId, oracionId, tipoVinculo))",
                "CREATE TABLE IF NOT EXISTS leccion_fuente_cache (leccionId INTEGER NOT NULL, fuenteId INTEGER NOT NULL, tipoVinculo TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(leccionId, fuenteId, tipoVinculo))",
                "CREATE TABLE IF NOT EXISTS leccion_cultura_cache (leccionId INTEGER NOT NULL, culturaId INTEGER NOT NULL, tipoVinculo TEXT NOT NULL, createdAtEpochMs INTEGER NOT NULL, nota TEXT, PRIMARY KEY(leccionId, culturaId, tipoVinculo))",
                "CREATE TABLE IF NOT EXISTS leccion_regla_gramatical_cache (leccionId INTEGER NOT NULL, reglaId INTEGER NOT NULL, orden INTEGER NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(leccionId, reglaId))",
                "CREATE TABLE IF NOT EXISTS leccion_regla_pronunciacion_cache (leccionId INTEGER NOT NULL, reglaId INTEGER NOT NULL, orden INTEGER NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(leccionId, reglaId))",
                "CREATE TABLE IF NOT EXISTS expresion_contexto_cultural_cache (expresionId INTEGER NOT NULL, culturaId INTEGER NOT NULL, tipoRelacion TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(expresionId, culturaId, tipoRelacion))",
                "CREATE TABLE IF NOT EXISTS traduccion_fuente_cache (traduccionId INTEGER NOT NULL, fuenteId INTEGER NOT NULL, tipoVinculo TEXT NOT NULL, nota TEXT, createdAtEpochMs INTEGER NOT NULL, PRIMARY KEY(traduccionId, fuenteId, tipoVinculo))",
                "CREATE TABLE IF NOT EXISTS palabra_canonica_cache (palabraId INTEGER NOT NULL PRIMARY KEY, palabraCanonicaId INTEGER NOT NULL, motivo TEXT NOT NULL, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE TABLE IF NOT EXISTS alineacion_curricular_cache (id INTEGER NOT NULL PRIMARY KEY, leccionId INTEGER NOT NULL, fuenteId INTEGER NOT NULL, unidad TEXT, tema TEXT NOT NULL, nivelReferencia TEXT, evidencia TEXT, estadoValidacion TEXT NOT NULL, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, tipoAlineacion TEXT, gradoReferencia TEXT, areaCurricular TEXT, competencia TEXT, indicadorLogro TEXT, contenidoCurricular TEXT, ejeSear TEXT, referenciaDocumental TEXT, paginaReferencia TEXT, aptaRevisionFormal INTEGER NOT NULL)",
                "CREATE INDEX IF NOT EXISTS index_alineacion_curricular_cache_leccionId ON alineacion_curricular_cache(leccionId)",
                "CREATE INDEX IF NOT EXISTS index_alineacion_curricular_cache_fuenteId ON alineacion_curricular_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS tuki_respuesta_sistema_cache (id INTEGER NOT NULL PRIMARY KEY, codigo TEXT NOT NULL, tipo TEXT NOT NULL, idiomaRespuestaId INTEGER, texto TEXT NOT NULL, prioridad INTEGER NOT NULL, activa INTEGER NOT NULL, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE UNIQUE INDEX IF NOT EXISTS index_tuki_respuesta_sistema_cache_codigo ON tuki_respuesta_sistema_cache(codigo)",
                "CREATE INDEX IF NOT EXISTS index_tuki_respuesta_sistema_cache_idiomaRespuestaId ON tuki_respuesta_sistema_cache(idiomaRespuestaId)",
                "CREATE TABLE IF NOT EXISTS logro_cache (id INTEGER NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, descripcion TEXT NOT NULL, condicionTipo TEXT NOT NULL, condicionValor INTEGER NOT NULL, categoriaId INTEGER)",
                "CREATE TABLE IF NOT EXISTS revision_linguistica_cache (id INTEGER NOT NULL PRIMARY KEY, tipoEntidad TEXT NOT NULL, entidadId INTEGER NOT NULL, clasificacion TEXT, estado TEXT NOT NULL, observacion TEXT, revisadoPor TEXT, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE INDEX IF NOT EXISTS index_revision_linguistica_cache_tipoEntidad_entidadId ON revision_linguistica_cache(tipoEntidad, entidadId)",
                "CREATE INDEX IF NOT EXISTS index_revision_linguistica_cache_estado ON revision_linguistica_cache(estado)",
                "CREATE TABLE IF NOT EXISTS usuario_actual_cache (id TEXT NOT NULL PRIMARY KEY, nombreUsuario TEXT, correo TEXT, edad INTEGER, pais TEXT, ciudad TEXT, idiomaMetaId INTEGER, xp INTEGER NOT NULL, rachaActual INTEGER NOT NULL, rachaMaxima INTEGER NOT NULL, ultimaActividad TEXT, nombre TEXT, apellido TEXT)",
                "CREATE TABLE IF NOT EXISTS progreso_leccion_usuario_cache (usuarioId TEXT NOT NULL, leccionId INTEGER NOT NULL, estado TEXT NOT NULL, puntaje INTEGER, fechaCompletadoEpochMs INTEGER, PRIMARY KEY(usuarioId, leccionId))",
                "CREATE TABLE IF NOT EXISTS palabra_favorita_usuario_cache (usuarioId TEXT NOT NULL, palabraId INTEGER NOT NULL, PRIMARY KEY(usuarioId, palabraId))",
                "CREATE TABLE IF NOT EXISTS logro_desbloqueado_usuario_cache (usuarioId TEXT NOT NULL, logroId INTEGER NOT NULL, fechaEpochMs INTEGER NOT NULL, PRIMARY KEY(usuarioId, logroId))",
                "CREATE TABLE IF NOT EXISTS replica_supabase_cache (tabla TEXT NOT NULL, clave TEXT NOT NULL, json TEXT NOT NULL, updatedAtEpochMs INTEGER NOT NULL, PRIMARY KEY(tabla, clave))",
                "CREATE INDEX IF NOT EXISTS index_replica_supabase_cache_tabla ON replica_supabase_cache(tabla)",
                "CREATE INDEX IF NOT EXISTS index_replica_supabase_cache_updatedAtEpochMs ON replica_supabase_cache(updatedAtEpochMs)"
            )
            statements.forEach(db::execSQL)
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val statements = listOf(
                "CREATE TABLE IF NOT EXISTS categoria_linguistica_cache (id INTEGER NOT NULL PRIMARY KEY, codigo TEXT NOT NULL, nombre TEXT NOT NULL, descripcion TEXT, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE UNIQUE INDEX IF NOT EXISTS index_categoria_linguistica_cache_codigo ON categoria_linguistica_cache(codigo)",
                "CREATE INDEX IF NOT EXISTS index_categoria_linguistica_cache_fuenteId ON categoria_linguistica_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS acepcion_categoria_linguistica_cache (acepcionId INTEGER NOT NULL, categoriaId INTEGER NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, PRIMARY KEY(acepcionId, categoriaId))",
                "CREATE INDEX IF NOT EXISTS index_acepcion_categoria_linguistica_cache_categoriaId ON acepcion_categoria_linguistica_cache(categoriaId)",
                "CREATE INDEX IF NOT EXISTS index_acepcion_categoria_linguistica_cache_fuenteId ON acepcion_categoria_linguistica_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS raiz_verbal_cache (id INTEGER NOT NULL PRIMARY KEY, acepcionId INTEGER NOT NULL, idiomaId INTEGER NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, regularidad TEXT, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE UNIQUE INDEX IF NOT EXISTS index_raiz_verbal_cache_acepcionId_textoNormalizado ON raiz_verbal_cache(acepcionId, textoNormalizado)",
                "CREATE INDEX IF NOT EXISTS index_raiz_verbal_cache_idiomaId ON raiz_verbal_cache(idiomaId)",
                "CREATE INDEX IF NOT EXISTS index_raiz_verbal_cache_fuenteId ON raiz_verbal_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS forma_verbal_documentada_cache (id INTEGER NOT NULL PRIMARY KEY, raizId INTEGER NOT NULL, palabraId INTEGER, reglaId INTEGER, codigoForma TEXT NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE INDEX IF NOT EXISTS index_forma_verbal_documentada_cache_raizId ON forma_verbal_documentada_cache(raizId)",
                "CREATE INDEX IF NOT EXISTS index_forma_verbal_documentada_cache_palabraId ON forma_verbal_documentada_cache(palabraId)",
                "CREATE INDEX IF NOT EXISTS index_forma_verbal_documentada_cache_reglaId ON forma_verbal_documentada_cache(reglaId)",
                "CREATE UNIQUE INDEX IF NOT EXISTS index_forma_verbal_documentada_cache_raizId_codigoForma_textoNormalizado ON forma_verbal_documentada_cache(raizId, codigoForma, textoNormalizado)",
                "CREATE INDEX IF NOT EXISTS index_forma_verbal_documentada_cache_fuenteId ON forma_verbal_documentada_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS excepcion_regla_gramatical_cache (reglaId INTEGER NOT NULL, tipoEntidad TEXT NOT NULL, entidadId INTEGER NOT NULL, motivo TEXT NOT NULL, formaDocumentadaId INTEGER, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, PRIMARY KEY(reglaId, tipoEntidad, entidadId))",
                "CREATE INDEX IF NOT EXISTS index_excepcion_regla_gramatical_cache_formaDocumentadaId ON excepcion_regla_gramatical_cache(formaDocumentadaId)",
                "CREATE INDEX IF NOT EXISTS index_excepcion_regla_gramatical_cache_fuenteId ON excepcion_regla_gramatical_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS marca_gramatical_documentada_cache (reglaId INTEGER NOT NULL, tipoEntidad TEXT NOT NULL, entidadId INTEGER NOT NULL, codigoMarca TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, PRIMARY KEY(reglaId, tipoEntidad, entidadId, codigoMarca))",
                "CREATE INDEX IF NOT EXISTS index_marca_gramatical_documentada_cache_tipoEntidad_entidadId ON marca_gramatical_documentada_cache(tipoEntidad, entidadId)",
                "CREATE INDEX IF NOT EXISTS index_marca_gramatical_documentada_cache_reglaId ON marca_gramatical_documentada_cache(reglaId)",
                "CREATE INDEX IF NOT EXISTS index_marca_gramatical_documentada_cache_codigoMarca ON marca_gramatical_documentada_cache(codigoMarca)",
                "CREATE INDEX IF NOT EXISTS index_marca_gramatical_documentada_cache_fuenteId ON marca_gramatical_documentada_cache(fuenteId)",
                "CREATE TABLE IF NOT EXISTS componente_regla_gramatical_cache (id INTEGER NOT NULL PRIMARY KEY, reglaId INTEGER NOT NULL, tipoContexto TEXT NOT NULL, contextoId INTEGER NOT NULL, nombreComponente TEXT NOT NULL, tipoEntidadOrigen TEXT NOT NULL, entidadOrigenId INTEGER NOT NULL, campoOrigen TEXT NOT NULL, valorDocumentado TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)",
                "CREATE UNIQUE INDEX IF NOT EXISTS index_componente_regla_gramatical_cache_reglaId_tipoContexto_contextoId_nombreComponente ON componente_regla_gramatical_cache(reglaId, tipoContexto, contextoId, nombreComponente)",
                "CREATE INDEX IF NOT EXISTS index_componente_regla_gramatical_cache_tipoEntidadOrigen_entidadOrigenId ON componente_regla_gramatical_cache(tipoEntidadOrigen, entidadOrigenId)",
                "CREATE INDEX IF NOT EXISTS index_componente_regla_gramatical_cache_fuenteId ON componente_regla_gramatical_cache(fuenteId)"
            )
            statements.forEach(db::execSQL)
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE raiz_verbal_cache_v11 (id INTEGER NOT NULL PRIMARY KEY, acepcionId INTEGER, idiomaId INTEGER NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, regularidad TEXT, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, tipoRaiz TEXT, evidenciaId TEXT, segmentacionOriginal TEXT, segmentacionNormalizada TEXT, grafiaOriginal TEXT, pagina INTEGER, motivoRevision TEXT)")
            db.execSQL("INSERT INTO raiz_verbal_cache_v11 (id, acepcionId, idiomaId, texto, textoNormalizado, regularidad, estadoValidacion, fuenteId, createdAtEpochMs, updatedAtEpochMs) SELECT id, acepcionId, idiomaId, texto, textoNormalizado, regularidad, estadoValidacion, fuenteId, createdAtEpochMs, updatedAtEpochMs FROM raiz_verbal_cache")
            db.execSQL("DROP TABLE raiz_verbal_cache")
            db.execSQL("ALTER TABLE raiz_verbal_cache_v11 RENAME TO raiz_verbal_cache")
            db.execSQL("CREATE UNIQUE INDEX index_raiz_verbal_cache_acepcionId_textoNormalizado ON raiz_verbal_cache(acepcionId, textoNormalizado)")
            db.execSQL("CREATE UNIQUE INDEX index_raiz_verbal_cache_evidenciaId ON raiz_verbal_cache(evidenciaId)")
            db.execSQL("CREATE INDEX index_raiz_verbal_cache_idiomaId ON raiz_verbal_cache(idiomaId)")
            db.execSQL("CREATE INDEX index_raiz_verbal_cache_fuenteId ON raiz_verbal_cache(fuenteId)")

            db.execSQL("CREATE TABLE forma_verbal_documentada_cache_v11 (id INTEGER NOT NULL PRIMARY KEY, raizId INTEGER, palabraId INTEGER, reglaId INTEGER, codigoForma TEXT NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL, tipoEvidenciaRaiz TEXT, evidenciaId TEXT, segmentacionOriginal TEXT, segmentacionNormalizada TEXT, grafiaOriginal TEXT, pagina INTEGER, motivoRevision TEXT, regularidadDocumentada TEXT)")
            db.execSQL("INSERT INTO forma_verbal_documentada_cache_v11 (id, raizId, palabraId, reglaId, codigoForma, texto, textoNormalizado, estadoValidacion, fuenteId, createdAtEpochMs, updatedAtEpochMs) SELECT id, raizId, palabraId, reglaId, codigoForma, texto, textoNormalizado, estadoValidacion, fuenteId, createdAtEpochMs, updatedAtEpochMs FROM forma_verbal_documentada_cache")
            db.execSQL("DROP TABLE forma_verbal_documentada_cache")
            db.execSQL("ALTER TABLE forma_verbal_documentada_cache_v11 RENAME TO forma_verbal_documentada_cache")
            db.execSQL("CREATE INDEX index_forma_verbal_documentada_cache_raizId ON forma_verbal_documentada_cache(raizId)")
            db.execSQL("CREATE INDEX index_forma_verbal_documentada_cache_palabraId ON forma_verbal_documentada_cache(palabraId)")
            db.execSQL("CREATE INDEX index_forma_verbal_documentada_cache_reglaId ON forma_verbal_documentada_cache(reglaId)")
            db.execSQL("CREATE UNIQUE INDEX index_forma_verbal_documentada_cache_raizId_codigoForma_textoNormalizado ON forma_verbal_documentada_cache(raizId, codigoForma, textoNormalizado)")
            db.execSQL("CREATE UNIQUE INDEX index_forma_verbal_documentada_cache_evidenciaId ON forma_verbal_documentada_cache(evidenciaId)")
            db.execSQL("CREATE INDEX index_forma_verbal_documentada_cache_fuenteId ON forma_verbal_documentada_cache(fuenteId)")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS seleccion_traduccion_camara_cache (
                    conceptoEspanolNormalizado TEXT NOT NULL,
                    etiquetaInglesNormalizada TEXT NOT NULL,
                    idiomaDestinoId INTEGER NOT NULL,
                    palabraEspanolId INTEGER NOT NULL,
                    palabraDestinoId INTEGER NOT NULL,
                    tipoRelacion TEXT NOT NULL,
                    relacionId INTEGER,
                    acepcionOrigenId INTEGER,
                    acepcionDestinoId INTEGER,
                    estadoValidacion TEXT NOT NULL,
                    motivoRevision TEXT NOT NULL,
                    PRIMARY KEY(conceptoEspanolNormalizado, idiomaDestinoId)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_seleccion_traduccion_camara_cache_etiquetaInglesNormalizada_idiomaDestinoId ON seleccion_traduccion_camara_cache(etiquetaInglesNormalizada, idiomaDestinoId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_seleccion_traduccion_camara_cache_palabraEspanolId ON seleccion_traduccion_camara_cache(palabraEspanolId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_seleccion_traduccion_camara_cache_palabraDestinoId ON seleccion_traduccion_camara_cache(palabraDestinoId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_seleccion_traduccion_camara_cache_relacionId ON seleccion_traduccion_camara_cache(relacionId)"
            )
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE audio_pronunciacion_cache ADD COLUMN textoNormalizado TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE audio_pronunciacion_cache ADD COLUMN fuenteId INTEGER")
            db.execSQL("ALTER TABLE audio_pronunciacion_cache ADD COLUMN hablante TEXT")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_audio_pronunciacion_cache_idiomaCodigo_textoNormalizado ON audio_pronunciacion_cache(idiomaCodigo, textoNormalizado)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAikukisnaDatabase(@ApplicationContext context: Context): AikukisnaDatabase {
        return Room.databaseBuilder(
            context,
            AikukisnaDatabase::class.java,
            "aikukisna.db"
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13
        ).build()
    }

    @Provides
    fun provideIdiomaDao(db: AikukisnaDatabase) = db.idiomaDao()

    @Provides
    fun provideCategoriaDao(db: AikukisnaDatabase) = db.categoriaDao()

    @Provides
    fun provideFuenteDocumentoDao(db: AikukisnaDatabase) = db.fuenteDocumentoDao()

    @Provides
    fun providePalabraDao(db: AikukisnaDatabase) = db.palabraDao()

    @Provides
    fun provideAudioPronunciacionDao(db: AikukisnaDatabase) = db.audioPronunciacionDao()

    @Provides
    fun provideTraduccionDao(db: AikukisnaDatabase) = db.traduccionDao()

    @Provides
    fun provideLeccionDao(db: AikukisnaDatabase) = db.leccionDao()

    @Provides
    fun provideOracionEjemploDao(db: AikukisnaDatabase) = db.oracionEjemploDao()

    @Provides
    fun provideLeccionPalabraDao(db: AikukisnaDatabase) = db.leccionPalabraDao()

    @Provides
    fun provideContenidoLeccionDao(db: AikukisnaDatabase) = db.contenidoLeccionDao()

    @Provides
    fun provideContenidoGlobalDao(db: AikukisnaDatabase) = db.contenidoGlobalDao()

    @Provides
    fun provideCompletarLeccionPendienteDao(db: AikukisnaDatabase) = db.completarLeccionPendienteDao()

    @Provides
    fun provideMemoriaTukiLocalDao(db: AikukisnaDatabase) = db.memoriaTukiLocalDao()

    @Provides
    fun provideConocimientoLinguisticoDao(db: AikukisnaDatabase) = db.conocimientoLinguisticoDao()

    @Provides
    fun provideEvidenciaGramaticalDao(db: AikukisnaDatabase) = db.evidenciaGramaticalDao()

    @Provides
    fun provideSeleccionTraduccionCamaraDao(db: AikukisnaDatabase) =
        db.seleccionTraduccionCamaraDao()
}
