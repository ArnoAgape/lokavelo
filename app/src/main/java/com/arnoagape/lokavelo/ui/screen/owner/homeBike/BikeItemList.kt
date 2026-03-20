package com.arnoagape.lokavelo.ui.screen.owner.homeBike

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
import androidx.compose.material3.Surface
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
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.theme.lightBlue
import com.arnoagape.lokavelo.ui.theme.lightBlueText
import com.arnoagape.lokavelo.ui.theme.lightGreen
import com.arnoagape.lokavelo.ui.theme.lightGreenText
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

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
                    bike = bike,
                    badge = {
                        StatusBadge(
                            isRented = bike.rentalStart != null && bike.rentalEnd != null
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BikeItemRow(
    bike: Bike,
    modifier: Modifier = Modifier,
    priceOverride: Long? = null,
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    showServiceFee: Boolean = true,
    badge: (@Composable () -> Unit)? = null
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        BikeImage(bike)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

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

                badge?.invoke()
            }

            val priceToDisplay = when {
                priceOverride != null -> priceOverride

                startDate != null && endDate != null -> {
                    val days = ChronoUnit.DAYS
                        .between(startDate, endDate)
                        .toInt()
                        .coerceAtLeast(1)

                    val basePrice = calculateRentalPrice(
                        dayPrice = bike.priceInCents,
                        days = days,
                        twoDaysPrice = bike.priceTwoDaysInCents,
                        weekPrice = bike.priceWeekInCents,
                        monthPrice = bike.priceMonthInCents
                    )

                    if (showServiceFee) {
                        val serviceFee = (basePrice * SERVICE_FEE_RATE).roundToLong()
                        basePrice + serviceFee
                    } else {
                        basePrice
                    }
                }

                else -> bike.priceInCents
            }

            val formattedPrice = remember(priceToDisplay) {
                NumberFormat
                    .getCurrencyInstance(Locale.FRANCE)
                    .format(priceToDisplay / 100.0)
            }

            Text(
                text = if (startDate != null && endDate != null || priceOverride != null) {
                    formattedPrice
                } else {
                    stringResource(
                        R.string.price_per_day,
                        formattedPrice
                    )
                },
                style = MaterialTheme.typography.bodyMedium
            )

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
fun RentalStatusBadge(status: RentalStatus) {

    val backgroundColor = when (status) {
        RentalStatus.PENDING,
        RentalStatus.COUNTER_OFFER -> MaterialTheme.colorScheme.tertiaryContainer

        RentalStatus.ACCEPTED,
        RentalStatus.ACTIVE -> lightBlue

        RentalStatus.COMPLETED -> lightGreen

        RentalStatus.DECLINED,
        RentalStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }

    val textColor = when (status) {
        RentalStatus.PENDING,
        RentalStatus.COUNTER_OFFER -> MaterialTheme.colorScheme.onTertiaryContainer

        RentalStatus.ACCEPTED,
        RentalStatus.ACTIVE -> lightBlueText

        RentalStatus.COMPLETED -> lightGreenText

        RentalStatus.DECLINED,
        RentalStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        tonalElevation = 1.dp
    ) {
        Text(
            text = stringResource(status.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
            badge = {},
            startDate = LocalDate.of(2026, 2, 21),
            endDate = LocalDate.of(2026, 2, 28)
        )
    }
}