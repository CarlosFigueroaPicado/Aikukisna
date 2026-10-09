package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

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

    LaunchedEffect(viewModel.requiereSeleccionIdioma) {
        if (viewModel.requiereSeleccionIdioma) {
            onCamposValidos()
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
            text = t(R.string.register_crear_cuenta),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = t(R.string.register_comienza_tu_viaje),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                label = t(R.string.register_nombres_y_apellidos),
                style = InputStyle.Compact,
                leadingIcon = R.drawable.user
            )
            AikukisnaTextField(
                value = nombreUsuario,
                onValueChange = onNombreUsuarioChange,
                label = t(R.string.register_nombre_de_usuario),
                style = InputStyle.Compact,
                leadingIcon = R.drawable.user_square
            )
            AikukisnaTextField(
                value = email,
                onValueChange = onEmailChange,
                label = t(R.string.register_correo_electronico),
                style = InputStyle.Compact,
                leadingIcon = R.drawable.mail
            )
            AikukisnaTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = t(R.string.register_contrasena),
                placeholder = t(R.string.register_8_caract_mayuscula_numero_y),
                isPassword = true,
                style = InputStyle.Compact,
                leadingIcon = R.drawable.lock
            )
            AikukisnaTextField(
                value = confirmarPassword,
                onValueChange = onConfirmarPasswordChange,
                label = t(R.string.register_repetir_contrasena),
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
                text = t(R.string.register_registrarse),
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
                    text = t(R.string.register_ya_tienes_cuenta),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = t(R.string.register_iniciar_sesion),
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
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = t(R.string.register_o),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AikukisnaButton(
                text = t(R.string.register_continuar_con_google),
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
