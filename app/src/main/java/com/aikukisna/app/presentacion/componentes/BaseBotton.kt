package com.aikukisna.app.presentacion.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BaseBotton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = true,
        shape = RoundedCornerShape(12.dp),

    ){
        Column {
            Text (text = "Login")
        }
    }

}

@Preview
@Composable
fun PreviewBaseBotton(){
    BaseBotton(
        onClick = {}
    )
}