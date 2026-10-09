package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aikukisna.app.data.local.entity.AcepcionCategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.AcepcionEntity
import com.aikukisna.app.data.local.entity.CategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.ComponenteReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.ExcepcionReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.FormaVerbalDocumentadaEntity
import com.aikukisna.app.data.local.entity.MarcaGramaticalDocumentadaEntity
import com.aikukisna.app.data.local.entity.RaizVerbalEntity
import com.aikukisna.app.data.local.entity.ExpresionEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.ReglaGramaticalEntity
import com.aikukisna.app.data.gramatica.CatalogoReglasGramaticales
import com.aikukisna.app.data.gramatica.MotorGramaticalControladoImpl
import com.aikukisna.app.data.gramatica.RepositorioEvidenciaGramaticalRoom
import com.aikukisna.app.di.DatabaseModule
import com.aikukisna.app.domain.gramatica.ResultadoReglaGramatical
import com.aikukisna.app.domain.gramatica.ReferenciaEvidenciaGramatical
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EvidenciaGramaticalRoomTest {
    private lateinit var database: AikukisnaDatabase
    private lateinit var context: Context

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(NOMBRE_MIGRACION)
        database = Room.inMemoryDatabaseBuilder(
            context,
            AikukisnaDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun cerrar() {
        database.close()
        context.deleteDatabase(NOMBRE_MIGRACION)
    }

    @Test
    fun conservaEvidenciaSinCompletarRegularidadPorInferencia() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        dao.guardarCategorias(
            listOf(CategoriaLinguisticaEntity(1, "verbo", "Verbo", null, "validada", 1, 1, 1))
        )
        dao.guardarCategoriasAcepcion(
            listOf(AcepcionCategoriaLinguisticaEntity(10, 1, "validada", 1, 1, 1))
        )
        dao.guardarRaicesVerbales(
            listOf(RaizVerbalEntity(20, 10, 1, "kaik", "kaik", null, "validada", 1, 1, 1))
        )

        assertEquals(1, dao.obtenerCategoriasDeAcepcion(10).size)
        assertNull(dao.obtenerRaicesDeAcepcion(10).single().regularidad)
        val evidencia = RepositorioEvidenciaGramaticalRoom(dao).obtenerEvidencia(
            1,
            ReferenciaEvidenciaGramatical("acepcion", 10)
        )
        assertEquals("verbo", evidencia.categorias.single().codigo)
        assertEquals("kaik", evidencia.raices.single().texto)
    }

    @Test
    fun relacionaFormasExcepcionesMarcasYComponentesConSuEvidencia() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        dao.guardarRaicesVerbales(
            listOf(RaizVerbalEntity(20, 10, 1, "kaik", "kaik", "regular", "validada", 1, 1, 1))
        )
        dao.guardarFormasVerbales(
            listOf(
                FormaVerbalDocumentadaEntity(
                    30, 20, null, 1, "infinitivo", "kaikaia", "kaikaia", "validada", 1, 1, 1
                )
            )
        )
        dao.guardarExcepciones(
            listOf(
                ExcepcionReglaGramaticalEntity(
                    1, "raiz_verbal", 20, "Forma documentada prioritaria", 30, "validada", 1, 1, 1
                )
            )
        )
        dao.guardarMarcas(
            listOf(
                MarcaGramaticalDocumentadaEntity(
                    1, "raiz_verbal", 20, "VERBO_REGULAR", "validada", 1, 1, 1
                )
            )
        )
        dao.guardarComponentes(
            listOf(
                ComponenteReglaGramaticalEntity(
                    60,
                    1,
                    "raiz_verbal",
                    20,
                    "RAIZ_VERBAL_DOCUMENTADA",
                    "raiz_verbal",
                    20,
                    "texto",
                    "kaik",
                    "validada",
                    1,
                    1,
                    1
                )
            )
        )

        assertEquals("kaikaia", dao.obtenerFormasDeRaiz(20).single().texto)
        assertEquals(30L, dao.obtenerExcepciones(1, "raiz_verbal", 20).single().formaDocumentadaId)
        assertEquals("VERBO_REGULAR", dao.obtenerMarcas("raiz_verbal", 20).single().codigoMarca)
        assertEquals(
            "RAIZ_VERBAL_DOCUMENTADA",
            dao.obtenerComponentes(1, "raiz_verbal", 20).single().nombreComponente
        )
        val evidencia = RepositorioEvidenciaGramaticalRoom(dao).obtenerEvidencia(
            1,
            ReferenciaEvidenciaGramatical("raiz_verbal", 20)
        )
        assertEquals("kaikaia", evidencia.formas.single().texto)
        assertEquals("kaikaia", evidencia.excepciones.single().formaDocumentada?.texto)
    }

    @Test
    fun migracionNueveADiezConservaDatosYCreaElEsquemaGramatical() {
        abrirBase(version = 9) { db ->
            db.execSQL("CREATE TABLE contenido_preexistente (id INTEGER NOT NULL PRIMARY KEY, valor TEXT NOT NULL)")
            db.execSQL("INSERT INTO contenido_preexistente VALUES (1, 'conservar')")
        }.close()

        val helper = abrirBase(version = 10, migrar = true)
        try {
            helper.readableDatabase.query("SELECT valor FROM contenido_preexistente WHERE id = 1").use {
                it.moveToFirst()
                assertEquals("conservar", it.getString(0))
            }
            val tablas = listOf(
                "categoria_linguistica_cache",
                "acepcion_categoria_linguistica_cache",
                "raiz_verbal_cache",
                "forma_verbal_documentada_cache",
                "excepcion_regla_gramatical_cache",
                "marca_gramatical_documentada_cache",
                "componente_regla_gramatical_cache"
            )
            tablas.forEach { tabla ->
                helper.readableDatabase.query(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?",
                    arrayOf(tabla)
                ).use {
                    it.moveToFirst()
                    assertEquals("No se creó $tabla", 1, it.getInt(0))
                }
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun migracionDiezAOnceConservaEvidenciaYAdmiteTrazabilidadSinRelacionesInventadas() {
        abrirBase(version = 10) { db ->
            db.execSQL("CREATE TABLE raiz_verbal_cache (id INTEGER NOT NULL PRIMARY KEY, acepcionId INTEGER NOT NULL, idiomaId INTEGER NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, regularidad TEXT, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE forma_verbal_documentada_cache (id INTEGER NOT NULL PRIMARY KEY, raizId INTEGER NOT NULL, palabraId INTEGER, reglaId INTEGER, codigoForma TEXT NOT NULL, texto TEXT NOT NULL, textoNormalizado TEXT NOT NULL, estadoValidacion TEXT NOT NULL, fuenteId INTEGER, createdAtEpochMs INTEGER NOT NULL, updatedAtEpochMs INTEGER NOT NULL)")
            db.execSQL("INSERT INTO raiz_verbal_cache VALUES (20, 10, 1, 'kaik', 'kaik', NULL, 'documentada', 3, 1, 1)")
            db.execSQL("INSERT INTO forma_verbal_documentada_cache VALUES (30, 20, NULL, NULL, 'infinitivo', 'kaik aia', 'kaik aia', 'documentada', 3, 1, 1)")
        }.close()

        val helper = abrirBase(version = 11, migrar = true)
        try {
            helper.readableDatabase.query("SELECT acepcionId, texto, tipoRaiz, evidenciaId FROM raiz_verbal_cache WHERE id = 20").use {
                it.moveToFirst()
                assertEquals(10L, it.getLong(0))
                assertEquals("kaik", it.getString(1))
                assertEquals(true, it.isNull(2))
                assertEquals(true, it.isNull(3))
            }
            helper.readableDatabase.query("SELECT raizId, texto, segmentacionOriginal FROM forma_verbal_documentada_cache WHERE id = 30").use {
                it.moveToFirst()
                assertEquals(20L, it.getLong(0))
                assertEquals("kaik aia", it.getString(1))
                assertEquals(true, it.isNull(2))
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun migracionOnceADoceCreaSeleccionContextualDeCamara() {
        abrirBase(version = 11).close()

        val helper = abrirBase(version = 12, migrar = true)
        try {
            helper.readableDatabase.query(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'seleccion_traduccion_camara_cache'"
            ).use {
                it.moveToFirst()
                assertEquals(1, it.getInt(0))
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun pobladorConservadorEsIdempotenteYNoLlenaTablasSinEvidencia() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        val categorias = listOf(
            "auxiliar modal", "auxiliar modal", "auxiliar modal", "auxiliar modal",
            "auxiliar modal", "auxiliar modal", "auxiliar modal", "auxiliar modal",
            "auxiliar modal", "auxiliar modal", "sustantivo", "sustantivo", "sustantivo",
            "sustantivo", "sustantivo", "sustantivo", "sustantivo", "adverbio/sustantivo",
            "adverbio", "subordinador", "marcador de aspecto", "marcador de negación"
        )
        database.conocimientoLinguisticoDao().guardarAcepciones(
            categorias.mapIndexed { indice, categoria ->
                AcepcionEntity(
                    id = 72_867L + indice,
                    palabraId = 70_000 + indice,
                    numeroAcepcion = 1,
                    definicion = null,
                    contexto = null,
                    categoriaGramatical = categoria,
                    estadoValidacion = "documentada",
                    createdAtEpochMs = 1,
                    updatedAtEpochMs = 1
                )
            }
        )
        database.palabraDao().guardarTodas(
            listOf(16359, 730, 1207).map { id ->
                PalabraEntity(id, 1, "palabra-$id", null, 1, estadoValidacion = "documentada")
            }
        )
        database.contenidoGlobalDao().guardarReglasGramaticales(
            listOf(44L, 36L, 29L, 22L, 1L).map { id ->
                ReglaGramaticalEntity(
                    id, 1, "regla-$id", "categoria", "Regla $id", "Descripción",
                    null, null, true, 1, 1, "documentada", null, 1, 1
                )
            }
        )
        val expresiones = listOf(1448L, 1572L, 1344L, 8814L, 1598L, 1502L, 1599L, 1004L, 1224L)
        database.conocimientoLinguisticoDao().guardarExpresiones(
            expresiones.map { id ->
                ExpresionEntity(id, 3, "expresión-$id", "expresión-$id", "ejemplo", "documentada", 13, 1, 1)
            }
        )
        val poblador = PobladorEvidenciaGramatical(database, dao, context)
        dao.guardarCategorias(
            listOf(
                CategoriaLinguisticaEntity(
                    99, "auxiliar_modal", "auxiliar modal", "Conservada", "validada", 13, 2, 2
                )
            )
        )

        val primera = poblador.sembrar()
        val segunda = poblador.sembrar()

        assertEquals(143, primera.total)
        assertEquals(143, segunda.total)
        assertEquals(7, dao.contarCategorias())
        assertEquals(22, dao.contarRelacionesCategoria())
        assertEquals(3, dao.contarExcepciones())
        assertEquals(12, dao.contarMarcas())
        assertEquals("Conservada", dao.obtenerCategoriaPorCodigo("auxiliar_modal")?.descripcion)
        assertEquals(99L, dao.obtenerCategoriasDeAcepcion(72_867).single().categoriaId)
        assertEquals(48, dao.contarRaices())
        assertEquals(51, dao.contarFormasVerbales())
        assertEquals(0, dao.contarComponentes())
        val raiz = dao.obtenerRaizPorEvidencia("aprende_p09_base_kaik_infinitivo")!!
        assertEquals(null, raiz.acepcionId)
        assertEquals("base_flexiva", raiz.tipoRaiz)
        assertEquals("kaik aia", raiz.segmentacionOriginal)
        assertEquals(null, raiz.segmentacionNormalizada)
        assertEquals(3, raiz.fuenteId)
        assertEquals(9, raiz.pagina)
        val irregular = dao.obtenerFormaPorEvidencia("aprende_p21_irregular_kaia")!!
        assertEquals(null, irregular.raizId)
        assertEquals("irregular", irregular.regularidadDocumentada)
        val raizSalamanca = dao.obtenerRaizPorEvidencia("wani_p56_ra_k_aia")!!
        assertEquals("ra", raizSalamanca.texto)
        assertEquals("raiz_lexica", raizSalamanca.tipoRaiz)
        assertEquals("ra-k-aia", raizSalamanca.segmentacionOriginal)
        assertEquals(60, raizSalamanca.fuenteId)
        assertEquals(56, raizSalamanca.pagina)
        val formaSalamanca = dao.obtenerFormaPorEvidencia("wani_p56_ra_k_aia")!!
        assertEquals(raizSalamanca.id, formaSalamanca.raizId)
        assertEquals("ra-k-aia", formaSalamanca.texto)
        assertEquals(60, formaSalamanca.fuenteId)
        val srim = dao.obtenerRaizPorEvidencia("wani_p56_srimaia")!!
        assertEquals("srim", srim.texto)
        assertEquals("srim-aia", dao.obtenerFormaPorEvidencia("wani_p56_srimaia")?.texto)
    }

    @Test
    fun repositorioEntregaMarcasPeroMotorNoGeneraSinComponentes() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        dao.guardarMarcas(
            listOf(
                MarcaGramaticalDocumentadaEntity(
                    36, "expresion", 8814, "MODALIDAD_OBLIGACION_NECESIDAD", "documentada", 13, 1, 1
                )
            )
        )
        val repositorio = RepositorioEvidenciaGramaticalRoom(dao)
        val motor = MotorGramaticalControladoImpl(CatalogoReglasGramaticales(context), repositorio)

        val resultado = motor.generarDesdeRoom(
            "modal_mos",
            ReferenciaEvidenciaGramatical("expresion", 8814)
        )

        assertEquals(true, resultado is ResultadoReglaGramatical.NoAplicable)
    }

    @Test
    fun motorGeneraCuandoRoomContieneMarcaYTodosLosComponentes() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        dao.guardarMarcas(
            listOf(
                MarcaGramaticalDocumentadaEntity(
                    36, "expresion", 8814, "MODALIDAD_OBLIGACION_NECESIDAD", "documentada", 13, 1, 1
                )
            )
        )
        dao.guardarComponentes(
            listOf(
                ComponenteReglaGramaticalEntity(
                    100, 36, "expresion", 8814, "SUJETO_DOCUMENTADO",
                    "expresion", 8814, "sujeto_documentado", "Dem", "documentada", 13, 1, 1
                ),
                ComponenteReglaGramaticalEntity(
                    101, 36, "expresion", 8814, "VERBO_BASE_DOCUMENTADO",
                    "expresion", 8814, "verbo_base_documentado", "gat", "documentada", 13, 1, 1
                )
            )
        )
        val repositorio = RepositorioEvidenciaGramaticalRoom(dao)
        val motor = MotorGramaticalControladoImpl(CatalogoReglasGramaticales(context), repositorio)

        val resultado = motor.generarDesdeRoom(
            "modal_mos",
            ReferenciaEvidenciaGramatical("expresion", 8814)
        )

        assertEquals(true, resultado is ResultadoReglaGramatical.Generada)
        resultado as ResultadoReglaGramatical.Generada
        assertEquals("Dem mos gat", resultado.texto)
    }

    @Test
    fun excepcionDeRoomDetieneLaReglaAntesDeGenerar() = runBlocking {
        val dao = database.evidenciaGramaticalDao()
        dao.guardarExcepciones(
            listOf(
                ExcepcionReglaGramaticalEntity(
                    1, "palabra", 16359, "Conjugación propia documentada", null,
                    "documentada", 1, 1, 1
                )
            )
        )
        val repositorio = RepositorioEvidenciaGramaticalRoom(dao)
        val motor = MotorGramaticalControladoImpl(CatalogoReglasGramaticales(context), repositorio)

        val resultado = motor.generarDesdeRoom(
            "infinitivo_aia",
            ReferenciaEvidenciaGramatical("palabra", 16359)
        )

        assertEquals(true, resultado is ResultadoReglaGramatical.NoAplicable)
        assertEquals(
            true,
            (resultado as ResultadoReglaGramatical.NoAplicable).motivo.contains("excepción")
        )
    }

    @Test
    fun reglaNoEjecutablePermaneceBloqueadaConRoom() = runBlocking {
        val repositorio = RepositorioEvidenciaGramaticalRoom(database.evidenciaGramaticalDao())
        val motor = MotorGramaticalControladoImpl(CatalogoReglasGramaticales(context), repositorio)

        val resultado = motor.generarDesdeRoom(
            "grado_antes_adjetivo",
            ReferenciaEvidenciaGramatical("expresion", 1)
        )

        assertEquals(true, resultado is ResultadoReglaGramatical.Bloqueada)
    }

    @Test
    fun replicaSoloAceptaEvidenciaKriolAprobada() = runBlocking {
        val sembrador = SembradorReplicaSupabase(
            context,
            database,
            PobladorEvidenciaGramatical(database, database.evidenciaGramaticalDao(), context),
            PobladorCorpusCamara(database.seleccionTraduccionCamaraDao())
        )
        val aprobada = Json.parseToJsonElement(
            """{"id":23,"regla_id":18,"texto_idioma":"No kom hiar!","traduccion_espanol":"Don't come here!","fuente_id":13,"estado_validacion":"documentada","nota":null,"created_at":"2026-09-23T23:07:27Z","idioma_traduccion_id":4}"""
        ).jsonObject
        val pendiente = Json.parseToJsonElement(
            """{"id":22,"regla_id":18,"texto_idioma":"nou","traduccion_espanol":"now","fuente_id":13,"estado_validacion":"documentada","nota":null,"created_at":"2026-09-23T23:07:27Z","idioma_traduccion_id":4}"""
        ).jsonObject

        assertEquals(
            1,
            sembrador.actualizarDesdeSupabase("ejemplo_regla_gramatical", listOf(aprobada, pendiente))
        )
        database.openHelper.writableDatabase.query(
            "SELECT id FROM ejemplo_regla_gramatical_cache ORDER BY id"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(23L, cursor.getLong(0))
            assertEquals(false, cursor.moveToNext())
        }
    }

    private fun abrirBase(
        version: Int,
        migrar: Boolean = false,
        crear: ((SupportSQLiteDatabase) -> Unit)? = null
    ): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(version) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                crear?.invoke(db)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                if (migrar) {
                    when (oldVersion to newVersion) {
                        9 to 10 -> DatabaseModule.MIGRATION_9_10.migrate(db)
                        10 to 11 -> DatabaseModule.MIGRATION_10_11.migrate(db)
                        11 to 12 -> DatabaseModule.MIGRATION_11_12.migrate(db)
                    }
                }
            }
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(NOMBRE_MIGRACION)
                .callback(callback)
                .build()
        ).also { it.writableDatabase }
    }

    private companion object {
        const val NOMBRE_MIGRACION = "evidencia-gramatical-migration-test.db"
    }
}
