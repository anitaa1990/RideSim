package com.an.ridesim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.an.ridesim.R
import com.an.ridesim.ui.component.CustomTitle
import com.an.ridesim.ui.viewmodel.AddressFieldType
import com.google.android.libraries.places.api.model.AutocompletePrediction

/**
 * [RideInputSection] is the bottom sheet content UI.
 *
 * Responsibilities:
 * - Displays greeting + input section
 * - Shows pickup/drop text fields
 * - Shows suggestions based on input focus
 * - Delegates user typing, focus, and suggestion tap callbacks
 */

@Composable
fun RideInputSection(
    pickupText: String,
    dropText: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit,
    onFieldFocusChanged: (AddressFieldType) -> Unit,
    suggestions: List<AutocompletePrediction>,
    onSuggestionSelected: (AutocompletePrediction) -> Unit,
    modifier: Modifier = Modifier
) {
    // We use LazyColumn here (instead of Column + verticalScroll) because:
    // - It prevents "infinite height constraint" crashes inside BottomSheetScaffold
    // - It allows the bottom sheet to expand to full height when needed
    // - It supports scrolling when the content exceeds available height (eg. when keyboard is open)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0XFFF2F1F4))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .imePadding()
    ) {
        // Top greeting text
        item {
            CustomTitle(text = stringResource(R.string.ride_input_title))
        }

        // Input card for address entry
        item {
            AddressCard(
                pickupText = pickupText,
                dropText = dropText,
                onPickupChange = onPickupChange,
                onDropChange = onDropChange,
                onFieldFocusChanged = onFieldFocusChanged
            )
        }

        // Suggestion dropdown (below input card)
        if (suggestions.isNotEmpty()) {
            items(suggestions.size) { index ->
                val prediction = suggestions[index]
                SuggestionItem(
                    prediction = prediction,
                    onClick = { onSuggestionSelected(prediction) }
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0XFFF2F1F4))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {

    }
}

@Composable
fun AddressCard(
    pickupText: String,
    dropText: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit,
    onFieldFocusChanged: (AddressFieldType) -> Unit
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_arrow),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.height(80.dp)
            )

            Column {
                // Pickup field with clear button
                RideAddressField(
                    value = pickupText,
                    onValueChange = onPickupChange,
                    placeholder = stringResource(R.string.ride_input_pickup_placeholder),
                    showClearButton = true,
                    onFocused = { onFieldFocusChanged(AddressFieldType.PICKUP) }
                )

                DividerLine()

                // Drop field (no clear button needed)
                RideAddressField(
                    value = dropText,
                    onValueChange = onDropChange,
                    placeholder = stringResource(R.string.ride_input_drop_placeholder),
                    onFocused = { onFieldFocusChanged(AddressFieldType.DROP) }
                )
            }
        }
    }
}

@Composable
fun RideAddressField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showClearButton: Boolean = false,
    onFocused: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 18.dp, start = 8.dp, end = 8.dp)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
            },
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    innerTextField()
                }
                if (showClearButton && isFocused && value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SuggestionItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history_24),
                contentDescription = null,
                tint = Color(0xFF5F6368),
                modifier = Modifier
                    .size(30.dp)
            )

            Column(modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)) {
                Text(
                    text = prediction.getPrimaryText(null).toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = prediction.getSecondaryText(null).toString(),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5F6368)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
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
