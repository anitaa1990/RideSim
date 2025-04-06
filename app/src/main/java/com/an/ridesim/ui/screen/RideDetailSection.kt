package com.an.ridesim.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.an.ridesim.R
import com.an.ridesim.model.VehicleDetail
import com.an.ridesim.ui.theme.heading3TextStyle
import com.an.ridesim.ui.theme.subTitleTextStyle
import com.an.ridesim.ui.theme.tertiaryTextStyle
import com.an.ridesim.ui.theme.titleTextStyle
import com.an.ridesim.ui.viewmodel.RideViewModel.RideUiState

@Composable
fun RideDetailSection(
    uiState: RideUiState,
    onVehicleSelected: (VehicleDetail) -> Unit
) {
    val vehicleList = uiState.availableVehicles
    val rideUiModel = uiState.rideUiModel

    // We use LazyColumn here (instead of Column + verticalScroll) because:
    // - It prevents "infinite height constraint" crashes inside BottomSheetScaffold
    // - It allows the bottom sheet to expand to full height when needed
    // - It supports scrolling when the content exceeds available height (eg. when keyboard is open)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            .imePadding()
    ) {
        // Top greeting text
        item {
            RideDetailTitleSection(
                distanceInKm = rideUiModel.distanceInKm,
                durationInMinutes = rideUiModel.durationInMinutes
            )
        }

        // Vehicle Options List
        items(vehicleList.size) { index ->
            val vehicle = vehicleList[index]
            VehicleListItem(
                vehicle = vehicle,
                isSelected = uiState.selectedVehicle.vehicleType == vehicle.vehicleType,
                onClick = { onVehicleSelected(vehicle) }
            )
        }
    }
}

@Composable
private fun RideDetailTitleSection(
    distanceInKm: Double?,
    durationInMinutes: Int?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = stringResource(R.string.ride_detail_title),
            style = heading3TextStyle(color = MaterialTheme.colorScheme.onError)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Distance with Icon
        IconWithText(
            iconId = R.drawable.ic_distance,
            iconContentDesc = R.string.content_desc_distance,
            tintColor = Color.Unspecified,
            text = String.format(stringResource(R.string.ride_detail_distance), distanceInKm)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .background(MaterialTheme.colorScheme.outline, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Time with Icon
        IconWithText(
            iconId = R.drawable.ic_history_24,
            iconContentDesc = R.string.content_desc_time,
            tintColor = MaterialTheme.colorScheme.outlineVariant,
            text = String.format(stringResource(R.string.ride_detail_time), durationInMinutes)
        )
    }
}

@Composable
fun VehicleListItem(
    vehicle: VehicleDetail,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        border = BorderStroke(1.dp, borderColor)
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

            // Vehicle Name and Description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Vehicle name
                    Text(
                        text = stringResource(id = vehicle.displayNameId),
                        style = titleTextStyle()
                    )

                    // Price
                    Text(
                        text = String.format(stringResource(R.string.ride_detail_price), vehicle.price),
                        style = titleTextStyle()
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vehicle description
                    Text(
                        text = stringResource(id = vehicle.descriptionId),
                        style = subTitleTextStyle()
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dot separator
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(MaterialTheme.colorScheme.outline, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // People icon
                    Icon(
                        painter = painterResource(R.drawable.ic_people),
                        contentDescription = stringResource(id = R.string.content_desc_people),
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    // People Count
                    Text(
                        text = stringResource(vehicle.peopleCount),
                        style = subTitleTextStyle(),
                    )
                }
            }
        }
    }
}

@Composable
private fun IconWithText(
    @DrawableRes iconId: Int,
    @StringRes iconContentDesc: Int,
    tintColor: Color,
    text: String
) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = stringResource(iconContentDesc),
        modifier = Modifier.size(20.dp),
        tint = tintColor
    )

    Spacer(modifier = Modifier.width(5.dp))

    Text(
        text = text,
        style = tertiaryTextStyle()
    )
}
