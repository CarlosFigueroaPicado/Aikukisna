package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.AikukisnaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        val itemsTab = listOf(
            Icons.Default.Home,
            Icons.Default.School,
            Icons.Default.ImportContacts,
            Icons.Default.AccountCircle
        )

        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.secondary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex = selectedIndex),
                    height = 4.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

        ) {
            itemsTab.forEachIndexed { index, icon ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },

                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selectedIndex == index) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewNavBar() {
    AikukisnaTheme {
        NavBar( selectedIndex = 1, onTabSelected = {})
    }

}



