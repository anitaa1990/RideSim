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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.an.ridesim.R
import com.an.ridesim.ui.model.RideStatusUiModel
import com.an.ridesim.ui.model.RideUiModel
import com.an.ridesim.ui.theme.heading3TextStyle
import com.an.ridesim.ui.theme.headlineStyle
import com.an.ridesim.ui.theme.subTitleTextStyle
import com.an.ridesim.util.RideUtils

@Composable
fun RideStatusOverlay(
    rideUiModel: RideUiModel,
    rideStatusUiModel: RideStatusUiModel
) {
    val textRes = if (rideStatusUiModel.hasDriverArrived)
        R.string.trip_status_arrived_title
    else R.string.trip_status_title

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
                            text = String.format(
                                stringResource(R.string.trip_distance),
                                rideStatusUiModel.distanceToTarget
                            ),
                            style = headlineStyle(color = MaterialTheme.colorScheme.onPrimary),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.trip_status_kms),
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
                        text = String.format(stringResource(textRes), rideUiModel.driverName),
                        style = subTitleTextStyle(color = MaterialTheme.colorScheme.onPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(
                            stringResource(R.string.trip_status_otp),
                            rideUiModel.otp
                        ),
                        style = subTitleTextStyle(color = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RideStatusOverlayPreview() {
    RideStatusOverlay(
        rideUiModel = RideUiModel(
            rideId = RideUtils.generateRandomRideId(),
            driverName = RideUtils.getRandomDriverName(),
            distanceInKm = 11.0,
            durationInMinutes = 23,
            rideStartTimeString = RideUtils.getRideTimeFormatted(),
            otp = RideUtils.generateSixDigitOtp()
        ),
        rideStatusUiModel = RideStatusUiModel(
            hasDriverArrived = false,
            distanceToTarget = 1.0
        )
    )
}
