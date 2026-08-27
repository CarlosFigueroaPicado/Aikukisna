package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.InputStyle
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    LaunchedEffect(viewModel.registroExitoso, onRegistroExitoso) {
        if (viewModel.registroExitoso) {
            onRegistroExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Comienza tu viaje con el Miskito",
                style = MaterialTheme.typography.bodyMedium
            )

            Image(
                painter = painterResource(id = R.drawable.ic_ave_login),
                contentDescription = null,
                modifier = Modifier
                    .height(89.dp)
                    .padding(5.dp)
            )

            AikukisnaTextField(
                value = viewModel.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = "Nombre completo",
                style = InputStyle.Outlined,
                leadingIcon = com.aikukisna.app.R.drawable.user
            )

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Correo electrónico",
                style = InputStyle.Outlined,
                leadingIcon = com.aikukisna.app.R.drawable.mail
            )

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Contraseña",
                isPassword = true,
                style = InputStyle.Outlined,
                leadingIcon = com.aikukisna.app.R.drawable.lock
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AikukisnaButton(
                text = "Crear cuenta",
                onClick = { viewModel.intentarRegistro() },
                isLoading = viewModel.isLoading
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "¿Ya tienes cuenta? Iniciar sesión",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onIrALogin() }
            )
        }

        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}