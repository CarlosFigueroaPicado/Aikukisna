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
import com.aikukisna.app.presentacion.viewmodel.LoginViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BluePressed
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onIrARegistro: () -> Unit = {},
    onEntrarComoInvitado: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.loginExitoso, onLoginSuccess) {
        if (viewModel.loginExitoso) {
            onLoginSuccess()
        }
    }

    LoginScreenContenido(
        identificador = viewModel.identificador,
        onIdentificadorChange = viewModel::onIdentificadorChange,
        password = viewModel.password,
        onPasswordChange = viewModel::onPasswordChange,
        isLoading = viewModel.isLoading,
        isLoadingGoogle = viewModel.isLoadingGoogle,
        errorMessage = viewModel.errorMessage,
        onLoginClick = viewModel::intentarLogin,
        onIrARegistroClick = onIrARegistro,
        onEntrarComoInvitadoClick = onEntrarComoInvitado,
        onGoogleClick = { viewModel.iniciarSesionConGoogle(context) }
    )
}

@Composable
private fun LoginScreenContenido(
    identificador: String,
    onIdentificadorChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    isLoadingGoogle: Boolean,
    errorMessage: String?,
    onLoginClick: () -> Unit,
    onIrARegistroClick: () -> Unit,
    onEntrarComoInvitadoClick: () -> Unit,
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
            text = "Iniciar sesión",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¡Bienvenido de vuelta!",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 89.dp, height = 80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.widthIn(max = 298.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            AikukisnaTextField(
                value = identificador,
                onValueChange = onIdentificadorChange,
                label = "Correo o nombre de usuario",
                style = InputStyle.Compact,
                leadingIcon = R.drawable.mail
            )
            AikukisnaTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                isPassword = true,
                style = InputStyle.Compact,
                leadingIcon = R.drawable.lock
            )
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.width(240.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AikukisnaButton(
                text = "Iniciar sesión",
                onClick = onLoginClick,
                isLoading = isLoading,
                style = ButtonStyle.Secondary,
                trailingIcon = R.drawable.arrow_right
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Regístrate",
                    style = MaterialTheme.typography.labelLarge,
                    color = BluePressed,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { onIrARegistroClick() }
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

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                AikukisnaButton(
                    text = "Entrar como invitado",
                    onClick = onEntrarComoInvitadoClick,
                    style = ButtonStyle.SecondaryGhost,
                    trailingIcon = R.drawable.clock_dashed
                )
                AikukisnaButton(
                    text = "Continuar con Google",
                    onClick = onGoogleClick,
                    isLoading = isLoadingGoogle,
                    style = ButtonStyle.SecondaryGhost,
                    trailingIcon = R.drawable.google,
                    trailingIconTintNatural = true
                )
            }
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
private fun LoginScreenContenidoPreview() {
    AikukisnaTheme {
        LoginScreenContenido(
            identificador = "", onIdentificadorChange = {},
            password = "", onPasswordChange = {},
            isLoading = false,
            isLoadingGoogle = false,
            errorMessage = null,
            onLoginClick = {},
            onIrARegistroClick = {},
            onEntrarComoInvitadoClick = {},
            onGoogleClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Con error")
@Composable
private fun LoginScreenContenidoConErrorPreview() {
    AikukisnaTheme {
        LoginScreenContenido(
            identificador = "davidf", onIdentificadorChange = {},
            password = "12345678", onPasswordChange = {},
            isLoading = false,
            isLoadingGoogle = false,
            errorMessage = "Usuario o contraseña incorrectos",
            onLoginClick = {},
            onIrARegistroClick = {},
            onEntrarComoInvitadoClick = {},
            onGoogleClick = {}
        )
    }
}