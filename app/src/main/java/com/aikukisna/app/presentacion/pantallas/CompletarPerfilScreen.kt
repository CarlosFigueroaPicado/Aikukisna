package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aikukisna.app.data.local.PerfilFotoStorage
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.viewmodel.CompletarPerfilViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import kotlinx.coroutines.launch

@Composable
fun CompletarPerfilScreen(
    viewModel: CompletarPerfilViewModel = hiltViewModel(),
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(viewModel.guardado) { if (viewModel.guardado) onGuardado() }
    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(t(R.string.completarperfil_completa_tu_informacion), style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground)
            Text(t(R.string.completarperfil_cerrar), Modifier.clickable(onClick = onVolver), color = MaterialTheme.colorScheme.primary)
        }
        Text(t(R.string.completarperfil_estos_datos_apareceran_en_tu), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FotoPerfil(uri = viewModel.fotoPerfilUri, onSeleccionar = { viewModel.fotoPerfilUri = it })
        AikukisnaTextField(viewModel.nombre, { viewModel.nombre = it }, label = t(R.string.completarperfil_nombres))
        AikukisnaTextField(viewModel.apellido, { viewModel.apellido = it }, label = t(R.string.completarperfil_apellidos))
        AikukisnaTextField(viewModel.nombreUsuario, { viewModel.nombreUsuario = it }, label = t(R.string.completarperfil_nombre_de_usuario))
        AikukisnaTextField(
            value = viewModel.usuario?.correo.orEmpty(),
            onValueChange = {},
            label = t(R.string.completarperfil_correo_electronico),
            enabled = false
        )
        AikukisnaTextField(viewModel.edad, { viewModel.edad = it }, label = t(R.string.completarperfil_edad), placeholder = t(R.string.completarperfil_ej_16))
        AikukisnaTextField(viewModel.pais, { viewModel.pais = it }, label = t(R.string.completarperfil_pais), placeholder = t(R.string.completarperfil_ej_nicaragua))
        AikukisnaTextField(viewModel.ciudad, { viewModel.ciudad = it }, label = t(R.string.completarperfil_ciudad_o_comunidad))
        viewModel.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(4.dp))
        AikukisnaButton(text = if (viewModel.isSaving) t(R.string.completarperfil_guardando) else t(R.string.completarperfil_guardar_informacion),
            onClick = viewModel::guardar, enabled = !viewModel.isSaving)
    }
}

@Composable
private fun FotoPerfil(uri: String?, onSeleccionar: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selected ->
        selected ?: return@rememberLauncherForActivityResult
        scope.launch {
            PerfilFotoStorage.guardar(context, selected)?.let(onSeleccionar)
        }
    }
    val bitmap by androidx.compose.runtime.produceState<android.graphics.Bitmap?>(null, uri) {
        value = uri?.let { PerfilFotoStorage.leerReducida(context, it) }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
            if (bitmap != null) Image(bitmap!!.asImageBitmap(), null, Modifier.fillMaxSize())
            else Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
        }
        Text(t(R.string.completarperfil_agregar_foto_de_perfil), Modifier.clickable { launcher.launch("image/*") }.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true, name = "Completar perfil")
@Composable
private fun CompletarPerfilScreenPreview() {
    AikukisnaTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Completa tu información", style = MaterialTheme.typography.headlineSmall)
            Text("Estos datos aparecerán en tu perfil y en Inicio.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FotoPerfil(uri = null, onSeleccionar = {})
            AikukisnaTextField("María", {}, label = "Nombres")
            AikukisnaTextField("López", {}, label = "Apellidos")
            AikukisnaTextField("maria_lopez", {}, label = "Nombre de usuario")
            AikukisnaTextField("maria@ejemplo.com", {}, label = "Correo electrónico", enabled = false)
            AikukisnaTextField("16", {}, label = "Edad")
            AikukisnaTextField("Nicaragua", {}, label = "País")
            AikukisnaTextField("Puerto Cabezas", {}, label = "Ciudad o comunidad")
            AikukisnaButton("Guardar información", {})
        }
    }
}
