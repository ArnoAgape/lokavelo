package com.arnoagape.lokavelo.ui.screen.main.detail.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike.sections.DetailRow
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import com.arnoagape.lokavelo.ui.utils.toEuroString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun PriceBreakdownCard(
    bike: Bike,
    startDate: LocalDate,
    endDate: LocalDate,
    serviceFeePercent: Int = 10
) {

    val days = ChronoUnit.DAYS.between(startDate, endDate)
        .toInt()
        .coerceAtLeast(1)

    val pricePerDayInCents = bike.priceInCents

    val subtotal = calculateRentalPrice(
        dayPrice = pricePerDayInCents,
        twoDaysPrice = bike.priceTwoDaysInCents,
        weekPrice = bike.priceWeekInCents,
        monthPrice = bike.priceMonthInCents,
        days = days
    )
    val original = pricePerDayInCents * days
    val discount = original - subtotal
    val serviceFee = subtotal * serviceFeePercent / 100
    val total = subtotal + serviceFee

    val dateFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRANCE)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        DetailRow(
            label = stringResource(R.string.period),
            value = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}",
            valueWeight = 2f
        )

        Spacer(Modifier.height(4.dp))

        // Prix normal
        PriceRow(
            label = pluralStringResource(
                R.plurals.price_per_day_times_days,
                days,
                pricePerDayInCents.toEuroString(),
                days
            ),
            value = original.toEuroString()
        )

        // Réduction (si applicable)
        if (discount > 0) {
            PriceRow(
                label = stringResource(R.string.discount_pricing),
                value = "- ${discount.toEuroString()}"
            )
        }

        // Frais service
        PriceRow(
            label = stringResource(
                R.string.service_fee_percent,
                serviceFeePercent
            ),
            value = serviceFee.toEuroString()
        )

        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            DividerDefaults.color
        )

        // Total
        PriceRow(
            label = stringResource(R.string.total_price_ttc),
            value = total.toEuroString(),
            isBold = true
        )
    }
}