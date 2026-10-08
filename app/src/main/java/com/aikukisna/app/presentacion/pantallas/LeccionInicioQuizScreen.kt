package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.presentacion.viewmodel.LeccionViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LeccionInicioQuizScreen(
    viewModel: LeccionViewModel = hiltViewModel(),
    leccionId: Int,
    onContinuar: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(leccionId) { viewModel.cargar(leccionId) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(onClick = onContinuar)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = t(R.string.leccioninicioquiz_volver),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp).clickable(onClick = onVolver)
            )
            Text(
                viewModel.tituloLeccion.uppercase().ifBlank { t(R.string.leccioninicioquiz_leccion) },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t(R.string.leccioninicioquiz_vocabulario), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                Text("5/5", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(t(R.string.leccioninicioquiz_preparate_para_empezar_el))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("quiz") }
                            append("!")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier.size(width = 233.dp, height = 100.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.scenes_lake),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Image(
                        painter = painterResource(id = R.drawable.tuki_lake),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 71.dp, height = 60.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LeccionInicioQuizScreenPreview() {
    AikukisnaTheme {
        LeccionInicioQuizScreen(leccionId = 1, onContinuar = {}, onVolver = {})
    }
}
