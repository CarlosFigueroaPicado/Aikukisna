package com.aikukisna.app.presentacion.componentes

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.AikukisnaTheme


enum class ButtonStyle { Primary, PrimaryGhost, Secondary, SecondaryGhost }

@Composable
fun AikukisnaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    // true para íconos que traen sus propios colores (el logo de Google,
    // 4 colores) — se dibujan con Image en vez de Icon, así no heredan el
    // tinte de un solo color que sí llevan el resto de los íconos del botón.
    trailingIconTintNatural: Boolean = false,
    // Sin especificar, hereda el tamaño de siempre (14sp, vía labelLarge).
    // Onboarding es hoy el único que pasa un valor distinto acá.
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
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
        ButtonStyle.PrimaryGhost -> ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonStyle.SecondaryGhost -> ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val border = when {
        !enabled -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
        style == ButtonStyle.PrimaryGhost -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        style == ButtonStyle.SecondaryGhost -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        else -> null
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "escalaAlPresionar"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        interactionSource = interactionSource,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        border = border,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = text, fontWeight = FontWeight.Bold, fontSize = fontSize)
                trailingIcon?.let { iconRes ->
                    if (trailingIconTintNatural) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                style = ButtonStyle.PrimaryGhost,
                leadingIcon = com.aikukisna.app.R.drawable.config,
                trailingIcon = com.aikukisna.app.R.drawable.config
            )
            AikukisnaButton(
                text = "Button",
                onClick = {},
                style = ButtonStyle.SecondaryGhost,
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