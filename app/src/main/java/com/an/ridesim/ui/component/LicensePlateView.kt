package com.an.ridesim.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LicensePlate(
    plateNumber: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Card(
            border = BorderStroke(2.dp, Color(0xFF454C55)),
            shape = RoundedCornerShape(3.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDCB6B)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(min = 30.dp)
        ) {
            Text(
                plateNumber,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF454C55)
                ),
                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally)
            )
        }
    }
}