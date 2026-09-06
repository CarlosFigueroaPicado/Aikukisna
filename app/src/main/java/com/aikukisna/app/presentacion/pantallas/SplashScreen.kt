package com.aikukisna.app.presentacion.pantallas

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aikukisna.app.ui.theme.AikukisnaTheme

/**
 * Pantalla de arranque real de la app (la parte que se puede controlar desde
 * Compose). El primer momento del arranque —el ícono "AK" + wordmark quietos—
 * NO vive acá: lo muestra el splash nativo de Android (Theme.Aikukisna.Splash
 * en themes.xml + installSplashScreen() en MainActivity), que es lo único
 * que llega a verse antes de que cualquier línea de Kotlin de esta pantalla
 * corra.
 *
 * Esta función arranca justo después de esa entrega y es un simple pasamano
 * a [LoadingScreen], que ahora es dueña de todo el tiempo de la pantalla de
 * carga (revelación del logo + fase de puntos cargando) y avisa sola cuándo
 * terminó — acá no hay que mantener un temporizador propio sincronizado a
 * mano con el de LoadingScreen.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    LoadingScreen(onFinished = onSplashFinished)
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    AikukisnaTheme {
        SplashScreen()
    }
}