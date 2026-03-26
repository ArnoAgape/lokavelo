package com.arnoagape.lokavelo.ui.screen.main.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import com.arnoagape.lokavelo.ui.utils.toEuroString
import java.time.temporal.ChronoUnit

@Composable
fun BikePreviewCard(
    bike: Bike,
    filters: SearchFilters,
    onBikeClick: (Bike) -> Unit,
    modifier: Modifier = Modifier
) {

    val hasDates = filters.startDate != null && filters.endDate != null

    val selectedDays = if (hasDates) {
        ChronoUnit.DAYS.between(filters.startDate, filters.endDate).toInt().coerceAtLeast(1)
    } else 0

    val isBelowMinDays = hasDates && selectedDays < bike.minDaysRental

    val priceText = if (!hasDates) {

        stringResource(
            R.string.price_per_day,
            bike.priceInCents.toEuroString()
        )

    } else {

        val start = filters.startDate
        val end = filters.endDate

        val days = ChronoUnit.DAYS
            .between(start, end)
            .toInt()
            .coerceAtLeast(1)

        val totalWithFees = (calculateRentalPrice(
            dayPrice = bike.priceInCents,
            days = days,
            twoDaysPrice = bike.priceTwoDaysInCents,
            weekPrice = bike.priceWeekInCents,
            monthPrice = bike.priceMonthInCents
        ) * (1 + SERVICE_FEE_RATE)).toLong()

        stringResource(
            R.string.price_total_with_daily,
            bike.priceInCents.toEuroString(),
            totalWithFees.toEuroString()
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onBikeClick(bike) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Row(Modifier.padding(12.dp)) {

            AsyncImage(
                model = bike.photoUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = bike.title,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )

                Text(
                    text = "${bike.location.postalCode} ${bike.location.city}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isBelowMinDays) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.min_days_rental_warning,
                            bike.minDaysRental,
                            bike.minDaysRental
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BikePreviewCardPreview() {
    LokaveloTheme {
        BikePreviewCard(
            bike = PreviewData.bike,
            filters = SearchFilters(),
            onBikeClick = {}
        )
    }
}