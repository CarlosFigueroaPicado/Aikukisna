package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    LaunchedEffect(viewModel.registroExitoso, onRegisterSuccess) {
        if (viewModel.registroExitoso) {
            onRegisterSuccess()
        }
        if (viewModel.errorMessage != null) {
            println("Error: ${viewModel.errorMessage}")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Comienza tu viaje con el Miskito",
                style = MaterialTheme.typography.bodyMedium
            )

            Image(
                painter = painterResource(id = com.aikukisna.app.R.drawable.ic_ave_login),
                contentDescription = "Logo",
                modifier = Modifier.height(89.dp).padding(5.dp)
            )

            AikukisnaTextField(
                value = viewModel.nombreUsuario,
                onValueChange = { viewModel.onNombreUsuarioChange(it) },
                label = "Nombre",
                leadingIcon = com.aikukisna.app.R.drawable.user,

            )

            AikukisnaTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Correo electrónico",
                leadingIcon = com.aikukisna.app.R.drawable.mail
            )

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Contraseña",
                isPassword = true,
                leadingIcon = com.aikukisna.app.R.drawable.lock
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodySmall
            )

        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AikukisnaButton(
                text = "Registrarse",
                onClick = { viewModel.intentarRegistro() },
                style = ButtonStyle.Secondary,
                trailingIcon = com.aikukisna.app.R.drawable.login

            )

            Row {
                Text(
                    text = "¿Ya tienes una cuenta? ",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Inicia sesión",
                    style = MaterialTheme.typography.bodySmall.copy
                        (color = MaterialTheme.colorScheme.primary),

                    modifier = Modifier.clickable {
                        onNavigateToLogin()
                    }
                )
            }
            Text(
                text = "O regístrate con",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaButton(
                text = "Entrar como invitado",
                onClick = {},
                style = ButtonStyle.Ghost,
                trailingIcon = com.aikukisna.app.R.drawable.clock_dashed
            )
            AikukisnaButton(
                text = "Continuar con Google",
                onClick = {},
                style = ButtonStyle.Ghost,
                trailingIcon = com.aikukisna.app.R.drawable.google
            )
        }

        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
