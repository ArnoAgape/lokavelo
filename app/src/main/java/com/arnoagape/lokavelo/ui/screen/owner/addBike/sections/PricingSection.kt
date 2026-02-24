package com.arnoagape.lokavelo.ui.screen.owner.addBike.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.calculateDiscountPercent
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull

@Composable
fun PricingSection(
    price: String,
    monthPrice: String,
    halfDayPrice: String,
    weekPrice: String,
    priceError: Boolean,
    onPriceChange: (String) -> Unit,
    onHalfDayChange: (String) -> Unit,
    onWeekChange: (String) -> Unit,
    onMonthChange: (String) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.pricing),
        subtitle = stringResource(R.string.subtitle_pricing)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            val dayPriceCents = price.toCentsOrNull()

            OutlinedTextField(
                value = price,
                isError = priceError,
                onValueChange = onPriceChange,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (priceError) {
                        Text(stringResource(R.string.required))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                label = { Text(stringResource(R.string.pricing_day_amount)) }
            )

            if (!price.isBlank()) {

                OutlinedTextField(
                    value = halfDayPrice,
                    onValueChange = onHalfDayChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    label = { Text(stringResource(R.string.pricing_half_day_amount)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weekPrice,
                    onValueChange = onWeekChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    label = { Text(stringResource(R.string.pricing_week_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        val weekCents = weekPrice.toCentsOrNull()
                        if (dayPriceCents != null && weekCents != null) {
                            val discount = calculateDiscountPercent(dayPriceCents, weekCents, 7)
                            Text(stringResource(R.string.discount, discount))
                        }
                    }
                )

                OutlinedTextField(
                    value = monthPrice,
                    onValueChange = onMonthChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    label = { Text(stringResource(R.string.pricing_month_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        val monthCents = monthPrice.toCentsOrNull()
                        if (dayPriceCents != null && monthCents != null) {
                            val discount = calculateDiscountPercent(dayPriceCents, monthCents, 30)
                            Text(stringResource(R.string.discount, discount))
                        }
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PricingSectionPreview() {
    LokaveloTheme {
        PricingSection(
            price = "20",
            monthPrice = "300",
            halfDayPrice = "10",
            weekPrice = "98",
            priceError = false,
            onPriceChange = {},
            onHalfDayChange = {},
            onWeekChange = {},
            onMonthChange = {}
        )
    }
}