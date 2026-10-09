package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aikukisna.app.data.repository.DiccionarioRepositoryImpl
import com.aikukisna.app.data.repository.RepositorioConocimientoImpl
import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.usecase.TraducirTextoUseCase
import com.aikukisna.app.domain.usecase.TukiOfflineResponder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConocimientoOfflineReplicaRoomTest {
    private lateinit var context: Context
    private lateinit var database: AikukisnaDatabase

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("replica_supabase_inicial", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        database = Room.inMemoryDatabaseBuilder(context, AikukisnaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrar() {
        database.close()
        context.getSharedPreferences("replica_supabase_inicial", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun replicaIncluidaResuelveDiccionarioTraductorYTukiSinClienteRemoto() = runBlocking {
        val sembrador = SembradorReplicaSupabase(
            context,
            database,
            PobladorEvidenciaGramatical(database, database.evidenciaGramaticalDao(), context),
            PobladorCorpusCamara(database.seleccionTraduccionCamaraDao())
        )

        sembrador.sembrarSiExiste()

        assertTrue(sembrador.hayContenidoDisponible())
        assertEquals(72_111L, contar("palabra_cache"))
        assertEquals(72_925L, contar("acepcion_cache"))
        assertEquals(132_350L, contar("traduccion_cache"))
        assertEquals(131_644L, contar("traduccion_acepcion_cache"))
        assertEquals(8_896L, contar("expresion_cache"))
        assertEquals(10_388L, contar("traduccion_expresion_cache"))
        assertEquals(1_010L, contar("oracion_ejemplo_cache"))

        val cache = CacheEscritor(
            context,
            database.idiomaDao(),
            database.categoriaDao(),
            database.fuenteDocumentoDao(),
            database.palabraDao(),
            database.traduccionDao(),
            database.leccionDao(),
            database.oracionEjemploDao(),
            database.leccionPalabraDao(),
            database.completarLeccionPendienteDao()
        )
        val diccionario = DiccionarioRepositoryImpl(cache)
        val conocimiento = RepositorioConocimientoImpl(
            diccionario,
            database.palabraDao(),
            database.conocimientoLinguisticoDao()
        )
        val traductor = TraducirTextoUseCase(conocimiento)
        val tuki = TukiOfflineResponder(diccionario, traductor)

        listOf("mesa", "libro", "puerta", "casa", "mujer", "agua", "carne").forEach { termino ->
            val coincidencias = diccionario.buscarPalabras(termino, IDIOMA_ESPANOL, 20)
            assertTrue(
                "La réplica local no resolvió $termino",
                coincidencias.any {
                    NormalizadorLinguistico.normalizar(it.texto) ==
                        NormalizadorLinguistico.normalizar(termino)
                }
            )
        }

        val frase = "Good morning teacher, may I ask a question?"
        val traduccion = traductor(frase, IDIOMA_INGLES_ESTANDAR, IDIOMA_ESPANOL)
        assertEquals("Buenos días maestro, ¿puedo hacer una pregunta?", traduccion.texto)

        val respuestaTuki = tuki.responder(
            "¿Qué significa Good morning teacher, may I ask a question? en inglés?",
            IDIOMA_INGLES_ESTANDAR
        )
        assertTrue(respuestaTuki.concluyente)
        assertTrue(respuestaTuki.texto.contains("Buenos días maestro"))
    }

    private fun contar(tabla: String): Long = database.openHelper.writableDatabase
        .query("SELECT COUNT(*) FROM `$tabla`")
        .use { cursor ->
            check(cursor.moveToFirst())
            cursor.getLong(0)
        }

    private companion object {
        const val IDIOMA_ESPANOL = 2
        const val IDIOMA_INGLES_ESTANDAR = 4
    }
}
