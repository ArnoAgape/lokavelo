package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.arnoagape.lokavelo.R

@Composable
fun DepositSection(
    deposit: String,
    onDepositChange: (String) -> Unit,
) {
    SectionCard(
        title = stringResource(R.string.deposit),
        subtitle = stringResource(R.string.subtitle_deposit)
    ) {

        OutlinedTextField(
            value = deposit,
            onValueChange = onDepositChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            label = { Text(stringResource(R.string.deposit_amount)) }
        )
    }

}