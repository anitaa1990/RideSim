package com.an.ridesim.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.an.ridesim.R
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.VehicleDetail
import com.an.ridesim.model.getIconId
import com.an.ridesim.model.getImageId
import com.an.ridesim.ui.component.DashedDivider
import com.an.ridesim.ui.component.LicensePlate
import com.an.ridesim.ui.model.LocationUiModel
import com.an.ridesim.ui.model.RideUiModel
import com.an.ridesim.ui.theme.primaryTextStyle
import com.an.ridesim.ui.theme.subTitleTextStyle
import com.an.ridesim.ui.theme.tertiaryTextStyle
import com.an.ridesim.ui.theme.titleTextStyle
import com.an.ridesim.util.RideUtils

@Composable
fun RideStartedSection(
    rideUiModel: RideUiModel,
    pickupLocation: LocationUiModel,
    dropLocation: LocationUiModel,
    vehicleDetail: VehicleDetail
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 10.dp, end = 10.dp, bottom = 16.dp)
    ) {
        // Vehicle Info Section
        VehicleInfoSection(
            rideUiModel = rideUiModel,
            vehicleDetail = vehicleDetail
        )

        // Fare estimation section
        FareEstimateSection(
            price = vehicleDetail.price
        )

        // Address section
        RideAddressSection(
            pickupLocation = pickupLocation,
            dropLocation = dropLocation
        )
    }
}

@Composable
private fun VehicleInfoSection(
    rideUiModel: RideUiModel,
    vehicleDetail: VehicleDetail
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp, start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier
                ) {
                    // Driver Avatar
                    Box(
                        modifier = Modifier
                            .width(65.dp)
                            .height(48.dp)
                            .background(Color.Transparent) // Outer box
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50)) // Blue border
                            .padding(3.dp) // <- Adds space between border and yellow bg
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50)) // Yellow fill
                            .clip(RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_driver),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Column {
                        // Driver name
                        Text(
                            text = rideUiModel.driverName,
                            style = titleTextStyle(),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Driver rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "5",
                                style = titleTextStyle(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dashed Divider
                DashedDivider()

                // Vehicle Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.55f).padding(vertical = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(vehicleDetail.vehicleType.getIconId()),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        vehicleDetail.vehicleType.name,
                        style = tertiaryTextStyle(color = MaterialTheme.colorScheme.outline)
                    )
                    Spacer(Modifier.width(15.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_people),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(vehicleDetail.peopleCount),
                        style = subTitleTextStyle(),
                    )
                }

            }

            Column(
                modifier = Modifier
            ) {
                Image(
                    painter = painterResource(vehicleDetail.vehicleType.getImageId()),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.End)
                )
                LicensePlate(
                    plateNumber = RideUtils.getRandomPlateForVehicleType(vehicleDetail.vehicleType),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun FareEstimateSection(
    price: Double
) {
    Column(
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 15.dp)
    ) {
        DashedDivider(width = 1f)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Fare estimate title
            Text(
                text = stringResource(id = R.string.ride_started_estimate_info),
                style = titleTextStyle()
            )

            // Total Price
            Text(
                text = String.format(stringResource(R.string.ride_detail_price), price),
                style = titleTextStyle(fontWeight = FontWeight.Bold)
            )
        }

        // Payment type
        Text(
            text = stringResource(id = R.string.ride_started_pay_info),
            style = subTitleTextStyle(),
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )

        DashedDivider(width = 1f)
    }
}

@Composable
private fun RideAddressSection(
    pickupLocation: LocationUiModel,
    dropLocation: LocationUiModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.height(110.dp)
        )

        Column {
            // Pickup Title
            Column(
                modifier = Modifier
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 15.dp)
            ) {
                Text(
                    text = pickupLocation.subLocality ?: "",
                    style = primaryTextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = pickupLocation.address ?: "",
                    style = tertiaryTextStyle(color = MaterialTheme.colorScheme.outline),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            DividerLine()

            // Pickup Title
            Column(modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp, bottom = 15.dp)) {
                Text(
                    text = dropLocation.subLocality ?: "",
                    style = primaryTextStyle(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = dropLocation.address ?: "",
                    style = tertiaryTextStyle(color = MaterialTheme.colorScheme.outline),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun VehicleInfoSectionPreview() {
    VehicleInfoSection(
        rideUiModel = RideUiModel(
            rideId = RideUtils.generateRandomRideId(),
            driverName = RideUtils.getRandomDriverName(),
            distanceInKm = 11.0,
            durationInMinutes = 23,
            rideStartTimeString = RideUtils.getRideTimeFormatted()
        ),
        vehicleDetail = VehicleDetail.getAuto()
    )
}

@Preview(showBackground = true)
@Composable
fun FareEstimateSectionPreview() {
    FareEstimateSection(
        price = 290.0
    )
}

@Preview(showBackground = true)
@Composable
fun RideAddressSectionPreview() {
    RideAddressSection(
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
