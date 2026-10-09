package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CorpusCamaraRoomTest {
    private lateinit var database: AikukisnaDatabase

    @Before
    fun preparar() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AikukisnaDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    @Throws(IOException::class)
    fun cerrar() {
        database.close()
    }

    @Test
    fun siembraTreintaConceptosSinGeneralizarCangrejoAKriol() = runBlocking {
        val dao = database.seleccionTraduccionCamaraDao()
        val poblador = PobladorCorpusCamara(dao)

        poblador.sembrar()
        poblador.sembrar()

        assertEquals(119, dao.contar())
        assertEquals(0, dao.contarCangrejoKriol())
        assertEquals(
            22354,
            dao.obtenerPorEtiqueta("mango", 1)?.palabraDestinoId
        )
        assertEquals(
            23414,
            dao.obtenerPorEtiqueta("monkey", 1)?.palabraDestinoId
        )
        assertEquals(
            64031,
            dao.obtenerPorConcepto("carne", 4)?.palabraDestinoId
        )
    }
}
