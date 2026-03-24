package com.arnoagape.lokavelo.ui.screen.bikes.bikeItem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.theme.lightBlue
import com.arnoagape.lokavelo.ui.theme.lightBlueText
import com.arnoagape.lokavelo.ui.theme.lightGreen
import com.arnoagape.lokavelo.ui.theme.lightGreenText
import kotlinx.coroutines.launch

@Composable
fun BikeItem(
    bikes: List<BikeWithRentals>,
    onBikeClick: (BikeWithRentals) -> Unit,
    selectionState: SelectionState,
    onToggleSelection: (String) -> Unit,
    onEnterSelectionMode: () -> Unit,
    contextBuilder: (BikeWithRentals) -> BikeItemContext
) {

    LazyColumn(contentPadding = PaddingValues(top = 10.dp)) {
        items(
            items = bikes,
            key = { it.bike.id }
        ) { item ->

            val context = contextBuilder(item)

            SelectItemRow(
                isSelectionMode = selectionState.isSelectionMode,
                isSelected = selectionState.selectedIds.contains(item.bike.id),
                onSelectToggle = { onToggleSelection(item.bike.id) },
                onClick = { onBikeClick(item) },
                onLongClick = {
                    if (!selectionState.isSelectionMode) onEnterSelectionMode()
                    onToggleSelection(item.bike.id)
                },
                badge = {
                    when (context) {

                        is BikeItemContext.OwnerGarage -> {
                            AvailabilityDotWithTooltip(context.bike.available)
                        }

                        is BikeItemContext.RenterRental -> {
                            RentalStatusBadge(context.rental.status)
                        }

                        is BikeItemContext.OwnerRental -> {
                            RentalStatusBadge(context.rental.status)
                        }

                        is BikeItemContext.ContactPreview -> Unit
                    }
                }
            ) {
                BikeItemRow(context = context)
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityDotWithTooltip(
    available: Boolean
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    val text = if (available) {
        stringResource(R.string.bike_availability_yes)
    } else {
        stringResource(R.string.bike_availability_no)
    }

    val color = if (available) lightGreenText else MaterialTheme.colorScheme.error

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
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(CircleShape)
                .background(color)
                .clickable {
                    scope.launch { tooltipState.show() }
                }
        )
    }
}