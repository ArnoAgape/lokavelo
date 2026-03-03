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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters
import com.arnoagape.lokavelo.ui.utils.calculateTotalPrice
import com.arnoagape.lokavelo.ui.utils.toEuroString
import java.time.temporal.ChronoUnit

@Composable
fun BikePreviewCard(
    bike: Bike,
    filters: SearchFilters,
    modifier: Modifier = Modifier
) {

    val hasDates = filters.startDate != null && filters.endDate != null

    val priceText = if (!hasDates) {

        stringResource(
            R.string.price_per_day,
            bike.priceInCents.toEuroString()
        )

    } else {

        val start = filters.startDate.toLocalDate()
        val end = filters.endDate.toLocalDate()
        val days = ChronoUnit.DAYS
            .between(start, end)
            .toInt()

        val total = calculateTotalPrice(
            days = days,
            dayPrice = bike.priceInCents,
            weekPrice = bike.priceWeekInCents,
            monthPrice = bike.priceMonthInCents
        )

        stringResource(
            R.string.price_total,
            total.toEuroString()
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                    maxLines = 1
                )

                Text(
                    text = "${bike.location.postalCode} ${bike.location.city}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}