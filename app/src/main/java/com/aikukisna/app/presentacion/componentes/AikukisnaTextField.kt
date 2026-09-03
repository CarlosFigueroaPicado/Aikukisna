package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BorderStrong
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.MediumGray


enum class InputStyle { Default, Outlined, Compact }

@Composable
fun AikukisnaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    style: InputStyle = InputStyle.Default,
    isError: Boolean = false,
    isPassword: Boolean = false,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    onDisabled: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null

) {

    var mostrarPassword by remember { mutableStateOf(false) }
    val visualTransformation = when {
        isPassword && mostrarPassword -> VisualTransformation.None
        isPassword -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val leadingIconLambda: (@Composable () -> Unit)? = leadingIcon?.let { iconRes ->
        {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    val trailingIconLambda: (@Composable () -> Unit)? = if (isPassword) {
        {
            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                Icon(
                    // Tachado = oculta (así se ve ahora); sin la línea = visible.
                    painter = painterResource(
                        id = if (mostrarPassword) R.drawable.eye_open else R.drawable.eye
                    ),
                    contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = if (mostrarPassword) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    } else {
        trailingIcon?.let { iconRes ->
            {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    val labelLambda: (@Composable () -> Unit)? = label?.let {
        { Text(text = it) }
    }

    val placeholderLambda: (@Composable () -> Unit)? = placeholder?.let {
        { Text(text = it) }
    }

    val combinedModifier = modifier
        .fillMaxWidth()
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                onFocused?.invoke()
            }
        }
        .then(
            if (!enabled && onDisabled != null) {
                Modifier.clickable { onDisabled() }
            } else {
                Modifier
            }
        )

    when (style) {
        InputStyle.Default -> {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = combinedModifier,
                enabled = enabled,
                label = labelLambda,
                placeholder = placeholderLambda,
                leadingIcon = leadingIconLambda,
                trailingIcon = trailingIconLambda,
                isError = isError,
                visualTransformation = visualTransformation,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.onTertiary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorLeadingIconColor = MaterialTheme.colorScheme.error,
                    errorTrailingIconColor = MaterialTheme.colorScheme.error
                )
            )
        }

        InputStyle.Outlined -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = combinedModifier,
                enabled = enabled,
                label = labelLambda,
                placeholder = placeholderLambda,
                leadingIcon = leadingIconLambda,
                trailingIcon = trailingIconLambda,
                isError = isError,
                visualTransformation = visualTransformation,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorLeadingIconColor = MaterialTheme.colorScheme.error,
                    errorTrailingIconColor = MaterialTheme.colorScheme.error
                )
            )
        }

        InputStyle.Compact -> {

            val interactionSource = remember { MutableInteractionSource() }

            val colores = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = BorderStrong,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MediumGray,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MediumGray,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MediumGray,
                cursorColor = MaterialTheme.colorScheme.primary
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = combinedModifier,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            ) { innerTextField ->

                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = labelLambda,
                    placeholder = placeholderLambda,
                    leadingIcon = leadingIconLambda,
                    trailingIcon = trailingIconLambda,
                    colors = colores,
                    contentPadding = OutlinedTextFieldDefaults.contentPadding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colores,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = false, name = "Modo Claro")
@Composable
fun PreviewAikukisnaTextField() {
    AikukisnaTheme(
        darkTheme = false,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16 .dp)
        ) {
            AikukisnaTextField(
                value = "Placeholder...",
                onValueChange = {},
                label = "Label",
                placeholder = "Placeholder",
                style = InputStyle.Default,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config

            )

            AikukisnaTextField(
                value = "Texto ingresado",
                onValueChange = {},
                label = "Etiqueta",
                placeholder = "Placeholder",
                style = InputStyle.Outlined,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )

            AikukisnaTextField(
                value = "Texto erróneo",
                onValueChange = {},
                label = "Etiqueta",
                placeholder = "Placeholder",
                style = InputStyle.Outlined,
                isError = true,
                onDisabled = {},
                onFocused = {},
                trailingIcon = com.aikukisna.app.R.drawable.error
            )

            AikukisnaTextField(
                value = "David",
                onValueChange = {},
                label = "Nombre de usuario",
                style = InputStyle.Compact,
                leadingIcon = com.aikukisna.app.R.drawable.user
            )

            AikukisnaTextField(
                value = "12345678",
                onValueChange = {},
                label = "Contraseña",
                style = InputStyle.Compact,
                isPassword = true,
                leadingIcon = com.aikukisna.app.R.drawable.lock
            )
        }
    }
}