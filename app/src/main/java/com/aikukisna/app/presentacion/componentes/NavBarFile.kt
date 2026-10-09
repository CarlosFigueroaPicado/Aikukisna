package com.aikukisna.app.presentacion.componentes

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R
import com.aikukisna.app.ui.theme.AikukisnaTheme

private data class ItemNav(
    val icono: Int,
    val etiqueta: String
)


// Se arma en cada uso: así las etiquetas siguen el idioma de la app elegido.
private val itemsNav get() = listOf(
    ItemNav(R.drawable.home, t(R.string.navbarfile_inicio)),
    ItemNav(R.drawable.ic_graduation_cap, t(R.string.navbarfile_aprender)),
    ItemNav(R.drawable.book_bookmark, t(R.string.navbarfile_diccionario)),
    ItemNav(R.drawable.user, t(R.string.navbarfile_perfil))
)

@Composable
fun NavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            itemsNav.forEachIndexed { index, item ->
                val seleccionado = selectedIndex == index
                val color = if (seleccionado) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = item.icono),
                        contentDescription = item.etiqueta,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = item.etiqueta,
                        color = color,
                        fontSize = 14.sp,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNavBar() {
    AikukisnaTheme {
        NavBar(selectedIndex = 0, onTabSelected = {})
    }
}
