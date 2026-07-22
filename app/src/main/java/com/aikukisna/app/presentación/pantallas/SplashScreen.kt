package com.aikukisna.app.presentación.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.Greeting
import com.aikukisna.app.ui.theme.AikukisnaTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {

    var progress by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(Unit) {

        val duration = 5000L
        val steps = 100

        repeat(steps) {
            delay(duration / steps)
            progress = (it + 1) / steps.toFloat()
        }

        onSplashFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1565C0))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo temporal
        Text(
            text = "🦜",
            fontSize = 90.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AIKUKISNA",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aprende Miskitu",
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(60.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreen() {
    AikukisnaTheme {
        Greeting("Android")
    }
}
