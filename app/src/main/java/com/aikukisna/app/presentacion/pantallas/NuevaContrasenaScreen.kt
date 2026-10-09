package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.componentes.InputStyle
import com.aikukisna.app.presentacion.viewmodel.NuevaContrasenaViewModel

@Composable
fun NuevaContrasenaScreen(
    viewModel: NuevaContrasenaViewModel,
    onCompletado: () -> Unit
) {
    LaunchedEffect(viewModel.completado) {
        if (viewModel.completado) onCompletado()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = t(R.string.nuevacontrasena_crear_nueva_contrasena),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = t(R.string.nuevacontrasena_escribe_y_confirma_la_contrasena),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 310.dp)
        )
        Spacer(Modifier.height(24.dp))
        AikukisnaTextField(
            value = viewModel.nuevaContrasena,
            onValueChange = viewModel::onNuevaContrasenaChange,
            label = t(R.string.nuevacontrasena_nueva_contrasena),
            style = InputStyle.Compact,
            isPassword = true,
            leadingIcon = R.drawable.lock
        )
        Spacer(Modifier.height(14.dp))
        AikukisnaTextField(
            value = viewModel.confirmacion,
            onValueChange = viewModel::onConfirmacionChange,
            label = t(R.string.nuevacontrasena_confirmar_contrasena),
            style = InputStyle.Compact,
            isPassword = true,
            leadingIcon = R.drawable.lock
        )
        Spacer(Modifier.height(18.dp))
        AikukisnaButton(
            text = t(R.string.nuevacontrasena_guardar_contrasena),
            onClick = viewModel::guardar,
            isLoading = viewModel.isLoading,
            style = ButtonStyle.Secondary
        )
        viewModel.errorMessage?.let { error ->
            Spacer(Modifier.height(14.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}
