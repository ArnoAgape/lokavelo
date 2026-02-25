package com.arnoagape.lokavelo.ui.screen.owner.addBike.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.AddressSuggestion

@Composable
fun LocationSection(
    addressLine: String,
    // addressLine2: String,
    zipCode: String,
    city: String,
    addressError: Boolean,
    zipCodeError: Boolean,
    cityError: Boolean,
    suggestions: List<AddressSuggestion>,
    onAddressLineChange: (String) -> Unit,
    // onAddressLine2Change: (String) -> Unit,
    onZipCodeChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.location),
        subtitle = stringResource(R.string.subtitle_location)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            AddressLineField(
                value = addressLine,
                addressError = addressError,
                suggestions = suggestions,
                onValueChange = onAddressLineChange,
                onSuggestionSelected = onSuggestionSelected
            )

            /*AddressLine2Field(
                value = addressLine2,
                onValueChange = onAddressLine2Change
            )*/

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZipCodeField(
                    modifier = Modifier.weight(0.4f),
                    value = zipCode,
                    zipCodeError = zipCodeError,
                    onValueChange = onZipCodeChange
                )

                CityField(
                    modifier = Modifier.weight(0.6f),
                    value = city,
                    cityError = cityError,
                    onValueChange = onCityChange
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressLineField(
    value: String,
    suggestions: List<AddressSuggestion>,
    addressError: Boolean,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit
) {

    val expanded = suggestions.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = true
                ),
            isError = addressError,
            supportingText = {
                if (addressError) {
                    Text(stringResource(R.string.required))
                }
            },
            label = {
                Text(
                    text = stringResource(R.string.address_line)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { }
        ) {
            suggestions.forEachIndexed { index, suggestion ->

                DropdownMenuItem(
                    onClick = {
                        onSuggestionSelected(suggestion)
                    },
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Column {

                                Text(
                                    text = suggestion.street
                                        ?: suggestion.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = buildString {
                                        suggestion.postalCode?.let {
                                            append(it)
                                            append(" ")
                                        }
                                        suggestion.city?.let { append(it) }
                                        suggestion.country?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                )

                if (index < suggestions.lastIndex) {
                    HorizontalDivider(
                        Modifier,
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/*@Composable
fun AddressLine2Field(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = stringResource(R.string.address_line_2_optional)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
}*/

@Composable
fun CityField(
    modifier: Modifier = Modifier,
    value: String,
    cityError: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        isError = cityError,
        supportingText = {
            if (cityError) {
                Text(stringResource(R.string.required))
            }
        },
        label = {
            Text(
                text = stringResource(R.string.city)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun ZipCodeField(
    modifier: Modifier = Modifier,
    value: String,
    zipCodeError: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        isError = zipCodeError,
        supportingText = {
            if (zipCodeError) {
                Text(stringResource(R.string.required))
            }
        },
        label = {
            Text(
                text = stringResource(R.string.zip_code),
                fontSize = 14.sp,
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    )
}