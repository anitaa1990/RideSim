package com.an.ridesim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.an.ridesim.R
import com.an.ridesim.ui.component.CustomTitle

@Composable
fun RideInputSection(
    pickupText: String,
    dropText: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0XFFF2F1F4))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CustomTitle(text = stringResource(R.string.ride_input_title))

        AddressCard(
            pickupText = pickupText,
            dropText = dropText,
            onPickupChange = onPickupChange,
            onDropChange = onDropChange
        )
    }
}

@Composable
fun AddressCard(
    pickupText: String,
    dropText: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_arrow),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxHeight(0.15f)
            )

            Column {
                RideAddressField(
                    value = pickupText,
                    onValueChange = onPickupChange,
                    placeholder = stringResource(R.string.ride_input_pickup_placeholder)
                )

                DividerLine()

                RideAddressField(
                    value = dropText,
                    onValueChange = onDropChange,
                    placeholder = stringResource(R.string.ride_input_drop_placeholder)
                )
            }
        }
    }
}

@Composable
fun RideAddressField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 18.dp, start = 8.dp, end = 8.dp),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 2.dp)
            .height(1.dp)
            .background(Color(0xFFE0E0E0))
    )
}

@Preview
@Composable
fun RideInputSectionPreview() {
    RideInputSection(
        pickupText = "Akshaya Tango",
        dropText = "",
        onDropChange = { },
        onPickupChange = { },
        modifier = Modifier
    )
}
