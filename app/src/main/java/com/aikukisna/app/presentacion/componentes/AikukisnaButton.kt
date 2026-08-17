package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.AikukisnaTheme


enum class ButtonStyle { Primary, Secondary, Ghost }

@Composable
fun AikukisnaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null
) {
    val colors = when (style) {
        ButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonStyle.Ghost -> ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val border = if (style == ButtonStyle.Ghost) {
        val borderColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        BorderStroke(1.dp, borderColor)
    } else null

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        border = border,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = LocalContentColor.current,
                                shape = CircleShape
                            )
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let { iconRes ->
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(text = text, fontWeight = FontWeight.Bold)
                trailingIcon?.let { iconRes ->
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

            }
        }
    }
}

@Preview(showBackground = false, name = "Modo Claro")
@Preview(showBackground = false, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Modo Oscuro",
)
@Composable
fun PreviewAikukisnaButton() {
    AikukisnaTheme {
        Column {
            AikukisnaButton(
                text = "Button",
                onClick = {},
                style = ButtonStyle.Primary,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )
            AikukisnaButton(
                text = "Button",
                onClick = {},
                style = ButtonStyle.Secondary,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )
            AikukisnaButton(
                text = "Button",
                onClick = {},
                style = ButtonStyle.Ghost,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )
            AikukisnaButton(
                text = "Cargando",
                onClick = {},
                isLoading = true,
                style = ButtonStyle.Primary,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )
        }
    }
}