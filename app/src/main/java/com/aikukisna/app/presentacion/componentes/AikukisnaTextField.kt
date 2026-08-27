package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.AikukisnaTheme

enum class InputStyle { Default, Outlined }

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
    val visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    val leadingIconLambda: (@Composable () -> Unit)? = leadingIcon?.let { iconRes ->
        {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    val trailingIconLambda: (@Composable () -> Unit)? = trailingIcon?.let { iconRes ->
        {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
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
        }
    }
}