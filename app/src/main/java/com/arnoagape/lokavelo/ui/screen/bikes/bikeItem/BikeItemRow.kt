package com.arnoagape.lokavelo.ui.screen.bikes.bikeItem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import java.text.NumberFormat
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun BikeItemRow(
    context: BikeItemContext,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val bike = when (context) {
                is BikeItemContext.OwnerGarage -> context.bike
                is BikeItemContext.RenterRental -> context.bike
                is BikeItemContext.OwnerRental -> context.bike
                is BikeItemContext.ContactPreview -> context.bike
            }

            BikeImage(bike)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bike.brand,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                when (context) {
                    is BikeItemContext.OwnerGarage -> {
                        OwnerGarageContent(context.bike)
                    }

                    is BikeItemContext.RenterRental -> {
                        RenterRentalContent(context)
                    }

                    is BikeItemContext.OwnerRental -> {
                        OwnerRentalContent(context)
                    }

                    is BikeItemContext.ContactPreview -> {
                        ContactPreviewContent(context)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactPreviewContent(context: BikeItemContext.ContactPreview) {

    val days = ChronoUnit.DAYS
        .between(context.startDate, context.endDate)
        .toInt()
        .coerceAtLeast(1)

    val price = calculateRentalPrice(
        dayPrice = context.bike.priceInCents,
        days = days,
        twoDaysPrice = context.bike.priceTwoDaysInCents,
        weekPrice = context.bike.priceWeekInCents,
        monthPrice = context.bike.priceMonthInCents
    )

    val formatted = remember(price) {
        NumberFormat.getCurrencyInstance(Locale.FRANCE)
            .format(price / 100.0)
    }

    Text(text = formatted)
    RentalDates(start = context.startDate, end = context.endDate)
}

@Composable
private fun OwnerGarageContent(bike: Bike) {
    val formattedPrice = remember(bike.priceInCents) {
        NumberFormat.getCurrencyInstance(Locale.FRANCE)
            .format(bike.priceInCents / 100.0)
    }
    Text(
        text = stringResource(R.string.price_per_day, formattedPrice),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun RenterRentalContent(context: BikeItemContext.RenterRental) {
    val days = ChronoUnit.DAYS
        .between(context.startDate, context.endDate)
        .toInt()
        .coerceAtLeast(1)

    val basePrice = calculateRentalPrice(
        dayPrice = context.bike.priceInCents,
        days = days,
        twoDaysPrice = context.bike.priceTwoDaysInCents,
        weekPrice = context.bike.priceWeekInCents,
        monthPrice = context.bike.priceMonthInCents
    )
    val totalPrice = remember(basePrice) {
        val serviceFee = (basePrice * SERVICE_FEE_RATE).roundToLong()
        NumberFormat.getCurrencyInstance(Locale.FRANCE)
            .format((basePrice + serviceFee) / 100.0)
    }

    Text(
        text = totalPrice,
        style = MaterialTheme.typography.bodyMedium
    )
    RentalDates(start = context.startDate, end = context.endDate)
}

@Composable
private fun OwnerRentalContent(context: BikeItemContext.OwnerRental) {
    val days = ChronoUnit.DAYS
        .between(context.startDate, context.endDate)
        .toInt()
        .coerceAtLeast(1)

    val netPrice = remember(days) {
        val basePrice = calculateRentalPrice(
            dayPrice = context.bike.priceInCents,
            days = days,
            twoDaysPrice = context.bike.priceTwoDaysInCents,
            weekPrice = context.bike.priceWeekInCents,
            monthPrice = context.bike.priceMonthInCents
        )
        NumberFormat.getCurrencyInstance(Locale.FRANCE)
            .format(basePrice / 100.0)
    }

    Text(
        text = netPrice,
        style = MaterialTheme.typography.bodyMedium
    )
    RentalDates(start = context.startDate, end = context.endDate)
}