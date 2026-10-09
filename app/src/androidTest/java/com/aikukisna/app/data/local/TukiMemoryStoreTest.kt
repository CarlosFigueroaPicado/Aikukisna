package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aikukisna.app.domain.repository.ContextoConversacionTuki
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TukiMemoryStoreTest {
    private lateinit var database: AikukisnaDatabase
    private lateinit var store: TukiMemoryStore

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AikukisnaDatabase::class.java).build()
        store = TukiMemoryStore(database.memoriaTukiLocalDao())
    }

    @After fun close() = database.close()

    @Test fun persisteMensajesYContextoDeLaPalabra() = runBlocking {
        val user = UUID.randomUUID()
        store.aprender(
            user,
            "¿Qué significa muihni?",
            "Significado verificado",
            ContextoConversacionTuki("idioma", "muihni", 7, "DEFINE_WORD")
        )

        assertEquals("muihni", store.obtenerContexto(user)?.ultimaPalabraOFrase)
        val messages = store.obtenerMensajesRecientes(user)
        assertEquals(2, messages.size)
        assertTrue(messages.first().texto.contains("muihni"))
    }
}
