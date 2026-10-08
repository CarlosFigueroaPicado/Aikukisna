package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.usecase.LogroConEstado
import com.aikukisna.app.presentacion.viewmodel.LogrosViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun LogrosScreen(
    viewModel: LogrosViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    LogrosScreenContenido(
        logros = viewModel.logros,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onVolver = onVolver
    )
}

@Composable
private fun LogrosScreenContenido(
    logros: List<LogroConEstado>,
    isLoading: Boolean,
    errorMessage: String?,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = t(R.string.logros_volver),
                modifier = Modifier.size(20.dp).clickable(onClick = onVolver)
            )
            Text(
                text = t(R.string.logros_mis_logros),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                val desbloqueados = logros.count { it.desbloqueado }
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 12.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t(R.string.logros_progreso_de_logros), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("$desbloqueados/${logros.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        val progreso = if (logros.isEmpty()) 0f else desbloqueados.toFloat() / logros.size
                        Box(
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(progreso).fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(logros) { item -> TarjetaLogro(item) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaLogro(item: LogroConEstado) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (item.desbloqueado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center
        ) {
            if (item.desbloqueado) {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(painter = painterResource(id = R.drawable.lock), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = item.logro.nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (item.desbloqueado) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = item.logro.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val logroDeMuestra = Logro(id = 1, nombre = "Primera lección", descripcion = "Completa tu primera lección", condicionTipo = "lecciones_completadas", condicionValor = 1, categoria = null)
private val logroDeMuestra2 = Logro(id = 2, nombre = "Racha de 7 días", descripcion = "Estudia 7 días consecutivos", condicionTipo = "racha_dias", condicionValor = 7, categoria = null)

@Preview(showBackground = true, name = "Con logros")
@Composable
private fun LogrosScreenContenidoPreview() {
    AikukisnaTheme {
        LogrosScreenContenido(
            logros = listOf(
                LogroConEstado(logroDeMuestra, desbloqueado = true, fecha = java.time.Instant.now()),
                LogroConEstado(logroDeMuestra2, desbloqueado = false, fecha = null)
            ),
            isLoading = false,
            errorMessage = null,
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun LogrosScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        LogrosScreenContenido(logros = emptyList(), isLoading = true, errorMessage = null, onVolver = {})
    }
}
