package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray


@Composable
fun GuestModeResultsScreen(
    idioma: Idioma,
    palabrasAprendidas: Int,
    respuestasCorrectas: Int,
    totalPreguntas: Int,
    onCrearCuenta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(12.dp))
                .background(color = CardSurface, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("¡Wali! ") }
                    append("(Excelente)")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.widthIn(max = 288.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Lo lograste!",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ya conoces tus primeras palabras en ${idioma.nombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                DetalleResultado(emoji = "\uD83D\uDCD6", valor = "$palabrasAprendidas", etiqueta = "palabras aprendidas")
                DetalleResultado(emoji = "\u2705", valor = "$respuestasCorrectas/$totalPreguntas", etiqueta = "respuestas correctas")
                DetalleResultado(emoji = "\u2B50", valor = "+50", etiqueta = "XP ganados")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 322.dp)
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(8.dp))
                .background(color = CardSurface, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Regístrate para desbloquear:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            BeneficioIcono(icono = null, emoji = "\uD83C\uDFC6", texto = "Logros y sistema de racha diaria")
            BeneficioIcono(icono = R.drawable.book_bookmark, emoji = null, texto = "Más de 300 palabras y 23 lecciones")
            BeneficioIcono(icono = R.drawable.camera, emoji = null, texto = "Cámara con IA - identifica objetos en ${idioma.nombre}")
            BeneficioIcono(icono = null, emoji = "\uD83D\uDD0A", texto = "Audio nativo con pronunciación real")
            BeneficioIcono(icono = R.drawable.refresh, emoji = null, texto = "Tu progreso sincronizado en todos tus dispositivos")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.width(240.dp)) {
            AikukisnaButton(
                text = "Crear cuenta",
                onClick = onCrearCuenta,
                trailingIcon = R.drawable.user_plus
            )
        }
    }
}

@Composable
private fun DetalleResultado(emoji: String, valor: String, etiqueta: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BeneficioIcono(icono: Int?, emoji: String?, texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            if (icono != null) {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = 22.sp)
            }
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestModeResultsScreenPreview() {
    AikukisnaTheme {
        GuestModeResultsScreen(
            idioma = Idioma.DISPONIBLES.first(),
            palabrasAprendidas = 5,
            respuestasCorrectas = 2,
            totalPreguntas = 2,
            onCrearCuenta = {}
        )
    }
}