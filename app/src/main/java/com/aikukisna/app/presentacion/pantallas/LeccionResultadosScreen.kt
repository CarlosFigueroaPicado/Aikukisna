package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.MediumGray

@Composable
fun LeccionResultadosScreen(
    respuestasCorrectas: Int,
    totalPreguntas: Int,
    onVolver: () -> Unit
) {

    val xpEstimado = 20 + respuestasCorrectas * 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier.size(width = 89.dp, height = 80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¡Lo lograste!",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$respuestasCorrectas de $totalPreguntas respuestas correctas",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandSubtle, RoundedCornerShape(16.dp))
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaResultado(valor = "$respuestasCorrectas/$totalPreguntas", etiqueta = "correctas")
            EstadisticaResultado(valor = "+$xpEstimado", etiqueta = "XP (aprox.)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.width(240.dp)) {
            AikukisnaButton(text = "Volver", onClick = onVolver, trailingIcon = R.drawable.home)
        }
    }
}

@Composable
private fun EstadisticaResultado(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = etiqueta, style = MaterialTheme.typography.bodySmall, color = MediumGray)
    }
}

@Preview(showBackground = true, name = "Resultado bueno")
@Composable
private fun LeccionResultadosScreenPreview() {
    AikukisnaTheme { LeccionResultadosScreen(respuestasCorrectas = 8, totalPreguntas = 8, onVolver = {}) }
}

@Preview(showBackground = true, name = "Resultado parcial")
@Composable
private fun LeccionResultadosScreenParcialPreview() {
    AikukisnaTheme { LeccionResultadosScreen(respuestasCorrectas = 3, totalPreguntas = 8, onVolver = {}) }
}