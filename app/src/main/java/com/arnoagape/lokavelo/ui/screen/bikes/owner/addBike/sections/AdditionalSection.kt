package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R

@Composable
fun AdditionalSection(
    availability: Boolean,
    minDaysRental: String,
    minDaysRentalError: Boolean,
    onAvailabilityChange: (Boolean) -> Unit,
    minDaysRentalChange: (String) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.additional_section),
        subtitle = stringResource(R.string.subtitle_additional_section)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.available_bike))

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = availability,
                    onCheckedChange = onAvailabilityChange
                )
            }

            OutlinedTextField(
                value = minDaysRental,
                onValueChange = minDaysRentalChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = minDaysRentalError,
                supportingText = {
                    if (minDaysRentalError) {
                        Text(stringResource(R.string.number_only))
                    }
                },
                label = { Text(stringResource(R.string.min_duration_rental)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.hint_min_days_rental),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            )
        }
    }
}