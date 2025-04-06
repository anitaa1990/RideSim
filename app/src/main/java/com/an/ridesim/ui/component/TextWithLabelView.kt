package com.an.ridesim.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextWithLabelView(
    label: String,
    text: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    )
    Spacer(modifier = Modifier.height(5.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
    )
}