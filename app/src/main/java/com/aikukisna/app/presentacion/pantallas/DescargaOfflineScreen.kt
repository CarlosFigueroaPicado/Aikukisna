package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.usecase.ProgresoPronunciaciones
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.ButtonStyle

@Composable
fun DescargaOfflineScreen(
    estado: EstadoSincronizacion?,
    progresoAudio: ProgresoPronunciaciones = ProgresoPronunciaciones(0, 0, 0),
    audioActivo: Boolean = false,
    onReintentar: () -> Unit
) {
    val progreso = (estado as? EstadoSincronizacion.EnProgreso)?.progreso ?: 0f
    val etapa = (estado as? EstadoSincronizacion.EnProgreso)?.etapa
    val error = (estado as? EstadoSincronizacion.Error)?.mensaje

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(12.dp))
        Image(
            painter = painterResource(R.drawable.tuki_thinking),
            contentDescription = null,
            modifier = Modifier.size(132.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                t(R.string.descargaoffline_tu_aprendizaje_tambien_sin_internet),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = etapa ?: t(R.string.descargaoffline_estamos_preparando_tus_lecciones_y),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(t(R.string.descargaoffline_contenido_offline), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    t(R.string.descargaoffline_una_sola_descarga_para_estudiar),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                if (error == null) {
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    RowProgress(progreso)
                    if (audioActivo || progresoAudio.total > 0) {
                        Spacer(Modifier.height(22.dp))
                        Text(t(R.string.descargaoffline_pronunciaciones), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            t(R.string.descargaoffline_las_palabras_quedan_disponibles_para),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { if (progresoAudio.total == 0) 0f else progresoAudio.completadas.toFloat() / progresoAudio.total },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        RowAudioProgress(progresoAudio)
                    }
                } else {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    AikukisnaButton(text = t(R.string.descargaoffline_reintentar_descarga), onClick = onReintentar, style = ButtonStyle.Secondary)
                }
            }
        }
        Text(t(R.string.descargaoffline_puedes_dejar_la_app_abierta), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RowAudioProgress(progreso: ProgresoPronunciaciones) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (progreso.omitidas > 0) t(R.string.descargaoffline_audio_preparado_algunos_quedaran_bajo) else t(R.string.descargaoffline_preparando_audio),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text("${progreso.completadas}/${progreso.total}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun RowProgress(progreso: Float) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(t(R.string.descargaoffline_descargando_contenido), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(t(R.string.descargaoffline_texto, (progreso * 100).toInt()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
