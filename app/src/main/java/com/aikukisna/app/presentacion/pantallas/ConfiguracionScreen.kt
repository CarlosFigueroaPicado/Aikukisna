package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.MediumGray

@Composable
fun ConfiguracionScreen(
    onCompletarInformacion: () -> Unit,
    onCambiarIdioma: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Configuración", style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Perfil y preferencias", style = MaterialTheme.typography.bodyMedium, color = MediumGray)
        OpcionConfiguracion(
            titulo = "Completa tu información",
            descripcion = "Agrega tu nombre, usuario y foto de perfil",
            icono = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
            onClick = onCompletarInformacion
        )
        OpcionConfiguracion(
            titulo = "Idioma que estás aprendiendo",
            descripcion = "Cambia el idioma de tus lecciones y diccionario",
            icono = { Text("文", color = MaterialTheme.colorScheme.primary) },
            onClick = onCambiarIdioma
        )
        Text("Volver", modifier = Modifier.clickable(onClick = onVolver).padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun OpcionConfiguracion(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        androidx.compose.foundation.layout.Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { icono() }
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MediumGray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
    }
}

@Preview(showBackground = true, name = "Configuración")
@Composable
private fun ConfiguracionScreenPreview() {
    com.aikukisna.app.ui.theme.AikukisnaTheme {
        ConfiguracionScreen(onCompletarInformacion = {}, onCambiarIdioma = {}, onVolver = {})
    }
}
