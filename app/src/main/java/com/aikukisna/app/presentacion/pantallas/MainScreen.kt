package com.aikukisna.app.presentacion.pantallas

import android.R.attr.text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.presentacion.componentes.NavBar
import java.util.UUID

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavBar(
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeContent(usuario = Usuario(id = UUID.randomUUID() ,  nombre = "Juan", apellido = "juan",
                    nombreUsuario = null, correo = null, edad = null, pais = null, ciudad = "esteli", idiomaMeta = null, xp = 100, rachaActual = 0, rachaMaxima = 0, ultimaActividad = null))

                1 -> LearnContent(
                    lecciones = listOf(
                        Leccion(id = 1, titulo = "Lección 1", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 2, titulo = "Lección 2", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 3, titulo = "Lección 3", capituloNumero = 1, nivel = 0, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 4, titulo = "Lección 4", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 5, titulo = "Lección 5", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 6, titulo = "Lección 6", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 7, titulo = "Lección 7", capituloNumero = 1, nivel = 1, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 8, titulo = "Lección 8", capituloNumero = 1, nivel = 2, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                        Leccion(id = 9, titulo = "Lección 9", capituloNumero = 1, nivel = 2, categoria = null, idiomaMeta = com.aikukisna.app.domain.model.Idioma(1, "es", "Español")),
                    ),
                    onLeccionClick = {}
                )

                2 -> Text(
                    text = "Cultura e Historias",
                    modifier = Modifier.align(Alignment.Center)
                )

                3 -> PerfilContent(
                    nombre = "Pedro Pérez",
                    email = "pedro@ejemplo.com",
                    rachaDias = 7,
                    puntos = 450,
                    leccionesCompletadas = 12,
                    onCerrarSesion = {}
                )

                4 -> Text(
                    text = "Configuración de Perfil",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}