package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.domain.usecase.ObtenerQuizDemoUseCase
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.ui.theme.AikukisnaTheme

/** Igual que LeccionInicioQuizScreen, pero para la prueba del modo invitado. */
@Composable
fun GuestInicioQuizScreen(
    onContinuar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = t(R.string.guestinicioquiz_volver),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp).clickable(onClick = onVolver)
            )
            Spacer(Modifier.weight(1f))
            Text(t(R.string.guestinicioquiz_modo_invitado), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 16.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 26.dp)) {
                Text(
                    t(R.string.guestinicioquiz_listo_terminaste_tu_leccion_de),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(t(R.string.guestinicioquiz_ahora))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(t(R.string.guestinicioquiz_prueba_tu_conocimiento)) }
                            append(t(R.string.guestinicioquiz_con_preguntas, ObtenerQuizDemoUseCase.PREGUNTAS_DEMO))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(modifier = Modifier.size(width = 233.dp, height = 100.dp), contentAlignment = Alignment.TopCenter) {
                    Image(
                        painter = painterResource(id = R.drawable.scenes_lake),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Image(
                        painter = painterResource(id = R.drawable.tuki_lake),
                        contentDescription = null,
                        modifier = Modifier.size(width = 71.dp, height = 60.dp).align(Alignment.TopCenter)
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.width(240.dp)) {
                AikukisnaButton(text = t(R.string.guestinicioquiz_empezar_quiz), onClick = onContinuar, trailingIcon = R.drawable.play)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestInicioQuizScreenPreview() {
    AikukisnaTheme { GuestInicioQuizScreen(onContinuar = {}, onVolver = {}) }
}
