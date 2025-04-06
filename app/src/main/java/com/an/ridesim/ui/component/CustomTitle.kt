package com.an.ridesim.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.an.ridesim.ui.theme.headlineStyle

@Composable
fun CustomTitle(
    text: String
) {
    Text(
        text = text,
        style = headlineStyle(),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}
