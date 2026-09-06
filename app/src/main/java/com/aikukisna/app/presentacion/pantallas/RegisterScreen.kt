package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.AikukisnaTextField
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.componentes.InputStyle
import com.aikukisna.app.presentacion.viewmodel.RegisterViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.OrangePressed



@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: () -> Unit,
    onCamposValidos: () -> Unit,   // nuevo
    onIrALogin: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.registroExitoso, onRegistroExitoso) {
        if (viewModel.registroExitoso) {
            onRegistroExitoso()
        }
    }

    RegisterScreenContenido(
        nombre = viewModel.nombre,
        onNombreChange = viewModel::onNombreChange,
        nombreUsuario = viewModel.nombreUsuario,
        onNombreUsuarioChange = viewModel::onNombreUsuarioChange,
        email = viewModel.email,
        onEmailChange = viewModel::onEmailChange,
        password = viewModel.password,
        onPasswordChange = viewModel::onPasswordChange,
        confirmarPassword = viewModel.confirmarPassword,
        onConfirmarPasswordChange = viewModel::onConfirmarPasswordChange,
        isLoading = viewModel.isLoading,
        isLoadingGoogle = viewModel.isLoadingGoogle,
        errorMessage = viewModel.errorMessage,
        onRegistrarClick = { if (viewModel.validarCampos()) onCamposValidos() },
        onIrALoginClick = onIrALogin,
        onGoogleClick = { viewModel.iniciarSesionConGoogle(context) }
    )
}

@Composable
private fun RegisterScreenContenido(
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreUsuario: String,
    onNombreUsuarioChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmarPassword: String,
    onConfirmarPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    isLoadingGoogle: Boolean,
    errorMessage: String?,
    onRegistrarClick: () -> Unit,
    onIrALoginClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comienza tu viaje",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 74.dp, height = 60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.widthIn(max = 298.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AikukisnaTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = "Nombres y apellidos",
                style = InputStyle.Compact,
                leadingIcon = R.drawable.user
            )
            AikukisnaTextField(
                value = nombreUsuario,
                onValueChange = onNombreUsuarioChange,
                label = "Nombre de usuario",
                style = InputStyle.Compact,
                leadingIcon = R.drawable.user_square
            )
            AikukisnaTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Correo electrónico",
                style = InputStyle.Compact,
                leadingIcon = R.drawable.mail
            )
            AikukisnaTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                placeholder = "8+ caract., mayúscula, número y símbolo",
                isPassword = true,
                style = InputStyle.Compact,
                leadingIcon = R.drawable.lock
            )
            AikukisnaTextField(
                value = confirmarPassword,
                onValueChange = onConfirmarPasswordChange,
                label = "Repetir contraseña",
                isPassword = true,
                style = InputStyle.Compact,
                leadingIcon = R.drawable.lock
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.width(240.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AikukisnaButton(
                text = "Registrarse",
                onClick = onRegistrarClick,
                isLoading = isLoading,
                trailingIcon = R.drawable.arrow_right
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrangePressed,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { onIrALoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray)
                Text(
                    text = "ó",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaButton(
                text = "Continuar con Google",
                onClick = onGoogleClick,
                isLoading = isLoadingGoogle,
                style = ButtonStyle.PrimaryGhost,
                trailingIcon = R.drawable.google,
                trailingIconTintNatural = true
            )
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Vacío")
@Composable
private fun RegisterScreenContenidoPreview() {
    AikukisnaTheme {
        RegisterScreenContenido(
            nombre = "", onNombreChange = {},
            nombreUsuario = "", onNombreUsuarioChange = {},
            email = "", onEmailChange = {},
            password = "", onPasswordChange = {},
            confirmarPassword = "", onConfirmarPasswordChange = {},
            isLoading = false,
            isLoadingGoogle = false,
            errorMessage = null,
            onRegistrarClick = {},
            onIrALoginClick = {},
            onGoogleClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Con datos y error")
@Composable
private fun RegisterScreenContenidoConErrorPreview() {
    AikukisnaTheme {
        RegisterScreenContenido(
            nombre = "David Figueroa", onNombreChange = {},
            nombreUsuario = "davidf", onNombreUsuarioChange = {},
            email = "david@correo.com", onEmailChange = {},
            password = "12345678", onPasswordChange = {},
            confirmarPassword = "1234", onConfirmarPasswordChange = {},
            isLoading = false,
            isLoadingGoogle = false,
            errorMessage = "Las contraseñas no coinciden",
            onRegistrarClick = {},
            onIrALoginClick = {},
            onGoogleClick = {}
        )
    }
}