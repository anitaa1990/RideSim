package com.an.ridesim.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.an.ridesim.ui.theme.heading3TextStyle
import com.an.ridesim.ui.theme.headlineStyle
import com.an.ridesim.ui.theme.subTitleTextStyle

@Composable
fun TripStatusOverlay(
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(50)
                )
                .padding(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.inversePrimary, shape = CircleShape)
                        .padding(vertical = 10.dp, horizontal = 22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "533",
                            style = headlineStyle(color = MaterialTheme.colorScheme.onPrimary),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "METERS",
                            style = heading3TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                ) {
                    Text(
                        text = "RENGANATHAN R is your driver",
                        style = subTitleTextStyle(color = MaterialTheme.colorScheme.onPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "OTP • 12345",
                        style = subTitleTextStyle(color = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripStatusOverlayPreview() {
    TripStatusOverlay()
}
