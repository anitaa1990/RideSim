package com.an.ridesim.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.ridesim.R
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.VehicleDetail
import com.an.ridesim.ui.component.TextWithLabelView
import com.an.ridesim.ui.model.LocationUiModel
import com.an.ridesim.ui.model.RideUiModel
import com.an.ridesim.ui.theme.heading3TextStyle
import com.an.ridesim.ui.theme.headlineStyle
import com.an.ridesim.ui.theme.primaryTextStyle
import com.an.ridesim.ui.theme.subTitleTextStyle
import com.an.ridesim.ui.theme.tertiaryTextStyle
import com.an.ridesim.ui.theme.titleTextStyle
import com.an.ridesim.ui.viewmodel.RideViewModel
import com.an.ridesim.util.RideUtils

@Composable
fun RideSummaryScreen(
    viewModel: RideViewModel,
    onStartTripAgain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = stringResource(R.string.ride_summary_title),
            style = titleTextStyle(),
            modifier = Modifier.padding(10.dp)
        )

        // Ride Summary
        RideSummarySection(
            vehicle = uiState.selectedVehicle
        )

        // Vehicle Summary
        VehicleSummarySection(
            vehicle = uiState.selectedVehicle,
            rideUiModel = uiState.rideUiModel
        )

        // Location summary
        LocationSummarySection(
            rideUiModel = uiState.rideUiModel,
            pickupLocation = uiState.pickupLocation,
            dropLocation = uiState.dropLocation
        )

        // Go home
        TextButton(
            onClick = { onStartTripAgain() },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(15.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_home),
                style = primaryTextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ).copy(textDecoration = TextDecoration.Underline)
            )
        }
    }
}

@Composable
private fun RideSummarySection(
    vehicle: VehicleDetail
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Vehicle Icon
            Image(
                painter = painterResource(id = vehicle.iconResId),
                contentDescription = stringResource(id = vehicle.displayNameId),
                modifier = Modifier.size(60.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Trip details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Trip date + time
                    Text(
                        text = RideUtils.getCurrentDateTimeFormatted(),
                        style = primaryTextStyle(fontWeight = FontWeight.SemiBold)
                    )

                    // Total Price
                    Text(
                        text = String.format(
                            stringResource(R.string.ride_detail_price), vehicle.price
                        ),
                        style = headlineStyle(),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                // Trip type
                Text(
                    text = String.format(
                        stringResource(R.string.ride_summary_type), vehicle.vehicleType.name
                    ),
                    style = heading3TextStyle(),
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun VehicleSummarySection(
    vehicle: VehicleDetail,
    rideUiModel: RideUiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1.3f)
            ) {
                TextWithLabelView(
                    label = stringResource(R.string.vehicle_model_label),
                    text = vehicle.vehicleType.name
                )

                Spacer(Modifier.height(16.dp))

                TextWithLabelView(
                    label = stringResource(R.string.ride_distance_label),
                    text = String.format(
                        stringResource(R.string.ride_detail_distance),
                        rideUiModel.distanceInKm
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.ride_rating_label),
                    style = subTitleTextStyle()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(0.7f)
            ) {
                TextWithLabelView(
                    label = stringResource(R.string.ride_driver_label),
                    text = rideUiModel.driverName
                )

                Spacer(Modifier.height(16.dp))

                TextWithLabelView(
                    label = stringResource(R.string.ride_time_label),
                    text = String.format(
                        stringResource(R.string.ride_detail_time),
                        rideUiModel.durationInMinutes
                    )
                )

                Spacer(Modifier.height(16.dp))

                TextWithLabelView(
                    label = stringResource(R.string.ride_id_label),
                    text = rideUiModel.rideId
                )
            }
        }
    }
}

@Composable
private fun LocationSummarySection(
    rideUiModel: RideUiModel,
    pickupLocation: LocationUiModel?,
    dropLocation: LocationUiModel?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier
            .padding(
                bottom = 16.dp,
                start = 16.dp,
                top = 14.dp,
                end = 16.dp
            )
        ) {

            Text(
                text = stringResource(R.string.location_summary_title),
                style = heading3TextStyle()
            )

            Spacer(Modifier.height(15.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(90.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    // Pickup
                    LocationText(
                        timeString = rideUiModel.rideStartTimeString,
                        subLocality = pickupLocation?.subLocality,
                        address = pickupLocation?.address
                    )

                    Spacer(Modifier.height(16.dp))

                    // Drop
                    LocationText(
                        timeString = RideUtils.getRideTimeFormatted(),
                        subLocality = dropLocation?.subLocality,
                        address = dropLocation?.address
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationText(
    timeString: String,
    subLocality: String?,
    address: String?
) {
    Text(
        text = timeString,
        style = tertiaryTextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = subLocality ?: "",
        style = tertiaryTextStyle(),
        modifier = Modifier.padding(bottom = 1.dp)
    )
    Text(
        text = address ?: "",
        style = tertiaryTextStyle(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(showBackground = true)
@Composable
fun RideSummarySectionPreview() {
    RideSummarySection(
        vehicle = VehicleDetail.getAuto()
    )
}

@Preview(showBackground = true)
@Composable
fun VehicleSummarySectionPreview() {
    VehicleSummarySection(
        vehicle = VehicleDetail.getAuto(),
        rideUiModel = RideUiModel(
            rideId = RideUtils.generateRandomRideId(),
            driverName = RideUtils.getRandomDriverName(),
            distanceInKm = 11.0,
            durationInMinutes = 23,
            rideStartTimeString = RideUtils.getRideTimeFormatted()
        )
    )
}

@Preview(showBackground = true)
@Composable
fun LocationSummarySectionPreview() {
    LocationSummarySection(
        rideUiModel = RideUiModel(
            rideId = RideUtils.generateRandomRideId(),
            driverName = RideUtils.getRandomDriverName(),
            distanceInKm = 11.0,
            durationInMinutes = 23,
            rideStartTimeString = RideUtils.getRideTimeFormatted()
        ),
        pickupLocation = LocationUiModel(
            "Seevaram, Thoraipakkam, Chennai, Tamil Nadhu",
            "Thoraipakkam",
            LatLngPoint(12.1, 15.3)
        ),
        dropLocation = LocationUiModel(
            "8th Ave, Seashore Town, Panaiyur, Chennai, Tamil Nadu",
            "Panaiyur",
            LatLngPoint(13.5, 16.4)
        )
    )
}
