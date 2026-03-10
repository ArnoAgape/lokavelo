package com.arnoagape.lokavelo.ui.screen.owner.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.theme.lightBlue
import com.arnoagape.lokavelo.ui.theme.lightBlueText
import com.arnoagape.lokavelo.ui.theme.lightGreen
import com.arnoagape.lokavelo.ui.theme.lightGreenText
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun BikeItem(
    bikes: List<Bike>,
    onBikeClick: (Bike) -> Unit,
    selectionState: SelectionState,
    onToggleSelection: (String) -> Unit,
    onEnterSelectionMode: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(top = 10.dp)) {
        items(
            items = bikes,
            key = { it.id }
        ) { bike ->

            SelectItemRow(
                id = bike.id,
                isSelectionMode = selectionState.isSelectionMode,
                isSelected = selectionState.selectedIds.contains(bike.id),
                onSelectToggle = { onToggleSelection(bike.id) },
                onClick = { onBikeClick(bike) },
                onLongClick = {
                    if (!selectionState.isSelectionMode) {
                        onEnterSelectionMode()
                    }
                    onToggleSelection(bike.id)
                }
            ) {
                BikeItemRow(
                    bike = bike
                )
            }
        }
    }
}

@Composable
fun BikeItemRow(
    bike: Bike,
    modifier: Modifier = Modifier,
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    showStatus: Boolean = true
) {
    val isRented = bike.rentalStart != null && bike.rentalEnd != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 📷 Photo
        BikeImage(bike)

        // 📝 Contenu texte
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // Ligne titre + pastille
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bike.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (showStatus) StatusBadge(isRented)
            }

            val totalPrice = if (startDate != null && endDate != null) {
                val days = ChronoUnit.DAYS
                    .between(startDate, endDate)
                    .toInt()
                    .coerceAtLeast(1)

                calculateRentalPrice(
                    dayPrice = bike.priceInCents,
                    days = days,
                    twoDaysPrice = bike.priceTwoDaysInCents,
                    weekPrice = bike.priceWeekInCents,
                    monthPrice = bike.priceMonthInCents
                )
            } else null

            // 💰 Prix
            val formattedPrice = remember(bike.priceInCents) {
                val priceInEuros = bike.priceInCents / 100.0
                NumberFormat
                    .getCurrencyInstance(Locale.FRANCE)
                    .format(priceInEuros)
            }

            if (totalPrice != null) {

                val formattedTotal = remember(totalPrice) {
                    val euros = totalPrice / 100.0 * 1.1
                    NumberFormat.getCurrencyInstance(Locale.FRANCE).format(euros)
                }

                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.bodyMedium
                )

            } else {

                Text(
                    text = stringResource(
                        R.string.price_per_day,
                        formattedPrice
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 📅 Dates si location active
            if (startDate != null && endDate != null) {

                RentalDates(
                    start = startDate,
                    end = endDate
                )
            }
        }
    }
}

@Composable
fun StatusBadge(isRented: Boolean) {

    val backgroundColor = if (isRented)
        lightBlue
    else
        lightGreen

    val textColor = if (isRented)
        lightBlueText
    else
        lightGreenText

    val text = if (isRented)
        stringResource(R.string.bike_availability_renting)
    else
        stringResource(R.string.bike_availability_yes)

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun BikeImage(bike: Bike, size: Dp = 90.dp) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(R.drawable.pic_bike),
            contentDescription = bike.title,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = bike.photoUrls.firstOrNull() ?: R.drawable.pic_bike,
            contentDescription = bike.title,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@PreviewLightDark
@Composable
fun BikeItemRowPreview() {
    LokaveloTheme {
        BikeItemRow(
            bike = Bike(
                id = "1",
                title = "Origine Trail Explore",
                priceInCents = 2500,
                photoUrls = emptyList(),
                rentalStart = Instant.parse("2026-02-21T16:30:00Z"),
                rentalEnd = Instant.parse("2026-02-28T11:30:00Z")
            ),
            showStatus = false,
            startDate = LocalDate.of(2026, 2, 21),
            endDate = LocalDate.of(2026, 2, 28)
        )
    }
}