package com.arnoagape.lokavelo.ui.screen.bikes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.domain.model.priority
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.theme.lightBlue
import com.arnoagape.lokavelo.ui.theme.lightBlueText
import com.arnoagape.lokavelo.ui.theme.lightGreen
import com.arnoagape.lokavelo.ui.theme.lightGreenText
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun BikeItem(
    bikes: List<BikeWithRentals>,
    onBikeClick: (BikeWithRentals) -> Unit,
    selectionState: SelectionState,
    onToggleSelection: (String) -> Unit,
    onEnterSelectionMode: () -> Unit,
    contextBuilder: (BikeWithRentals) -> BikeItemContext  // ← nouveau
) {
    LazyColumn(contentPadding = PaddingValues(top = 10.dp)) {
        items(
            items = bikes,
            key = { it.bike.id }
        ) { item ->
            SelectItemRow(
                id = item.bike.id,
                isSelectionMode = selectionState.isSelectionMode,
                isSelected = selectionState.selectedIds.contains(item.bike.id),
                onSelectToggle = { onToggleSelection(item.bike.id) },
                onClick = { onBikeClick(item) },
                onLongClick = {
                    if (!selectionState.isSelectionMode) onEnterSelectionMode()
                    onToggleSelection(item.bike.id)
                }
            ) {
                BikeItemRow(context = contextBuilder(item))
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {

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

        badge?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                it()
            }
        }
    }
}

@Composable
fun AvailabilityDot(available: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                if (available) lightGreenText
                else MaterialTheme.colorScheme.error
            )
    )
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

@Composable
fun StatusDot(
    rentalStatus: RentalStatus?,
    onClick: (() -> Unit)? = null
) {

    val color = when (rentalStatus) {
        RentalStatus.ACTIVE -> lightBlue
        RentalStatus.ACCEPTED -> MaterialTheme.colorScheme.tertiary
        RentalStatus.PENDING,
        RentalStatus.COUNTER_OFFER -> MaterialTheme.colorScheme.secondary

        RentalStatus.DECLINED,
        RentalStatus.CANCELLED -> MaterialTheme.colorScheme.error

        RentalStatus.COMPLETED -> lightGreen
        null -> lightGreen
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDotWithTooltip(
    rentalStatus: RentalStatus?
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    val text = rentalStatus?.let {
        stringResource(it.labelRes())
    } ?: stringResource(R.string.bike_availability_yes)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = {
            PlainTooltip {
                Text(text)
            }
        },
        state = tooltipState
    ) {
        StatusDot(
            rentalStatus = rentalStatus,
            onClick = {
                scope.launch {
                    tooltipState.show()
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
fun BikeItemRowPreview() {
    LokaveloTheme {
        BikeItemRow(
            bike = PreviewData.bike,
            badge = {},
            startDate = LocalDate.of(2026, 2, 21),
            endDate = LocalDate.of(2026, 2, 28)
        )
    }
}