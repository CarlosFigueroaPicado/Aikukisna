package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.repository.AudioPronunciacionRepositoryImpl
import com.aikukisna.app.di.DatabaseModule
import com.aikukisna.app.domain.model.AudioPronunciacion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.OrigenAudioPronunciacion
import com.aikukisna.app.domain.model.ReferenciaAudioPronunciacion
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PalabraPronunciacionRoomTest {
    private lateinit var context: Context

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(NOMBRE_MIGRACION)
    }

    @After
    fun limpiar() {
        context.deleteDatabase(NOMBRE_MIGRACION)
    }

    @Test
    fun palabraAlmacenadaConMetadatosSeRecuperaSinPerderlos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AikukisnaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            db.idiomaDao().guardarTodos(listOf(IdiomaEntity(1, "mi", "Miskito")))
            db.fuenteDocumentoDao().guardarTodos(
                listOf(FuenteDocumentoEntity(1, "Fuente", null, null, null))
            )
            db.palabraDao().guardarTodas(
                listOf(PalabraEntity(1, 1, "Naksa", null, 1, "Nák-sa", "nak.sa", true))
            )

            val recuperada = db.palabraDao().obtenerPorId(1)!!
            assertEquals("Nák-sa", recuperada.pronunciacion)
            assertEquals("nak.sa", recuperada.pronunciacionFonetica)
            assertEquals(true, recuperada.pronunciacionVerificada)
        } finally {
            db.close()
        }
    }

    @Test
    fun migracionConservaPalabraYAplicaValoresIniciales() {
        abrirBase(version = 2) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS palabra_cache (
                    id INTEGER NOT NULL PRIMARY KEY,
                    idiomaId INTEGER NOT NULL,
                    texto TEXT NOT NULL,
                    categoriaId INTEGER,
                    fuenteId INTEGER NOT NULL
                )""".trimIndent()
            )
            db.execSQL("INSERT INTO palabra_cache VALUES (1, 1, 'Naksa', NULL, 1)")
        }.close()

        val helper = abrirBase(version = 3, migrar = true)
        try {
            helper.readableDatabase.query(
                "SELECT texto, pronunciacion, pronunciacion_fonetica, pronunciacion_verificada FROM palabra_cache WHERE id = 1"
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("Naksa", cursor.getString(0))
                assertNull(cursor.getString(1))
                assertNull(cursor.getString(2))
                assertFalse(cursor.getInt(3) != 0)
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun metadatosAsociadosSeConsultanCompletamenteOffline() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AikukisnaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val directorio = File(context.filesDir, "pronunciaciones_asociadas").apply { mkdirs() }
        val archivo = File(directorio, "naksa.wav").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        try {
            db.idiomaDao().guardarTodos(listOf(IdiomaEntity(1, "mi", "Miskito")))
            db.fuenteDocumentoDao().guardarTodos(
                listOf(FuenteDocumentoEntity(1, "Fuente", null, null, null))
            )
            db.palabraDao().guardarTodas(listOf(PalabraEntity(1, 1, "Naksa", null, 1)))
            val repositorio = AudioPronunciacionRepositoryImpl(
                context,
                db.audioPronunciacionDao(),
                SembradorAudiosHumanos(context, db.audioPronunciacionDao())
            )
            val miskito = Idioma(1, "mi", "Miskito")
            repositorio.registrar(
                AudioPronunciacion(
                    palabraId = 1,
                    idioma = miskito,
                    referencia = ReferenciaAudioPronunciacion.ArchivoInterno("naksa.wav"),
                    verificado = true,
                    origen = OrigenAudioPronunciacion.HUMANO
                )
            )

            val recuperado = repositorio.buscarVerificados(1, miskito).single()
            assertEquals(OrigenAudioPronunciacion.HUMANO, recuperado.origen)
            assertEquals(true, recuperado.verificado)
            assertEquals(true, repositorio.estaDisponibleOffline(recuperado))
            assertEquals(3, repositorio.leerAudio(recuperado)?.size)
        } finally {
            db.close()
            archivo.delete()
        }
    }

    @Test
    fun migracionTresACuatroConservaPalabrasYCreaRelacionDeAudio() {
        abrirBase(version = 3) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS palabra_cache (
                    id INTEGER NOT NULL PRIMARY KEY,
                    idiomaId INTEGER NOT NULL,
                    texto TEXT NOT NULL,
                    categoriaId INTEGER,
                    fuenteId INTEGER NOT NULL,
                    pronunciacion TEXT,
                    pronunciacion_fonetica TEXT,
                    pronunciacion_verificada INTEGER NOT NULL DEFAULT 0
                )""".trimIndent()
            )
            db.execSQL("INSERT INTO palabra_cache VALUES (1, 1, 'Naksa', NULL, 1, NULL, NULL, 0)")
        }.close()

        val helper = abrirBase(version = 4, migrar = true)
        try {
            helper.readableDatabase.query("SELECT texto FROM palabra_cache WHERE id = 1").use {
                it.moveToFirst()
                assertEquals("Naksa", it.getString(0))
            }
            helper.readableDatabase.query(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'audio_pronunciacion_cache'"
            ).use {
                it.moveToFirst()
                assertEquals(1, it.getInt(0))
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun migracionCuatroACincoCreaMemoriaLocalDeTuki() {
        abrirBase(version = 4).close()

        val helper = abrirBase(version = 5, migrar = true)
        try {
            helper.readableDatabase.query(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'memoria_tuki_local'"
            ).use {
                it.moveToFirst()
                assertEquals(1, it.getInt(0))
            }
        } finally {
            helper.close()
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
                    when {
                        oldVersion == 2 && newVersion == 3 -> DatabaseModule.MIGRATION_2_3.migrate(db)
                        oldVersion == 3 && newVersion == 4 -> DatabaseModule.MIGRATION_3_4.migrate(db)
                        oldVersion == 4 && newVersion == 5 -> DatabaseModule.MIGRATION_4_5.migrate(db)
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
        const val NOMBRE_MIGRACION = "pronunciacion-migration-test.db"
    }
}
