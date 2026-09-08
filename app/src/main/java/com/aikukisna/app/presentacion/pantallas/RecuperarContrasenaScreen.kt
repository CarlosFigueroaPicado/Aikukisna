package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.componentes.InputStyle
import com.aikukisna.app.presentacion.viewmodel.RecuperarContrasenaViewModel

@Composable
fun RecuperarContrasenaScreen(
    viewModel: RecuperarContrasenaViewModel,
    onVolver: () -> Unit
) {
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
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (viewModel.enviado) {
                "Revisa tu correo para continuar con el cambio de contraseña."
            } else {
                "Te enviaremos un enlace para crear una nueva contraseña."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 310.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (!viewModel.enviado) {
            AikukisnaTextField(
                value = viewModel.correo,
                onValueChange = viewModel::onCorreoChange,
                label = "Correo electrónico",
                style = InputStyle.Compact,
                leadingIcon = R.drawable.mail
            )
            Spacer(modifier = Modifier.height(18.dp))
            AikukisnaButton(
                text = "Enviar enlace",
                onClick = viewModel::solicitar,
                isLoading = viewModel.isLoading,
                style = ButtonStyle.Secondary
            )
        } else {
            AikukisnaButton(
                text = "Volver a iniciar sesión",
                onClick = onVolver,
                style = ButtonStyle.Secondary
            )
        }

        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Volver",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onVolver)
                .padding(8.dp)
        )
    }
}
