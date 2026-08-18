package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    LaunchedEffect(viewModel.loginExitoso, onLoginSuccess) {
        if (viewModel.loginExitoso) {
            onLoginSuccess()
        }
        if (viewModel.errorMessage != null) {
            println("Error: ${viewModel.errorMessage}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "!Bienvenido de nuevo!",
                style = MaterialTheme.typography.bodyMedium
            )

            Image(
                painter = painterResource(id = com.aikukisna.app.R.drawable.ic_ave_login),
                contentDescription = "Logo",
                modifier = Modifier.height(89.dp).padding(5.dp)
            )

            AikukisnaTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Correo electrónico",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Contraseña",
                isPassword = true,
                leadingIcon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AikukisnaButton(
                text = "Iniciar sesión",
                onClick = { viewModel.intentarLogin() },
                isLoading = viewModel.isLoading
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "¿No tienes una cuenta? Registrate",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "O inicia sesión con",
                style = MaterialTheme.typography.bodySmall
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
