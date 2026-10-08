package com.aikukisna.app.presentacion.pantallas

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aikukisna.app.ui.theme.AikukisnaTheme


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