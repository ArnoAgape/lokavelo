package com.arnoagape.lokavelo.ui.screen.owner.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.theme.lightBlue
import com.arnoagape.lokavelo.ui.theme.lightBlueText
import com.arnoagape.lokavelo.ui.theme.lightGreen
import com.arnoagape.lokavelo.ui.theme.lightGreenText
import com.arnoagape.lokavelo.ui.utils.Format.formatDate
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

@Composable
fun BikeItem(
    bikes: List<Bike>,
    onBikeClick: (Bike) -> Unit,
    selectionState: SelectionState,
    onToggleSelection: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = bikes,
            key = { it.id }
        ) { bike ->

            SelectItemRow(
                id = bike.id,
                isSelectionMode = selectionState.isSelectionMode,
                isSelected = selectionState.selectedIds.contains(bike.id),
                onSelectToggle = { onToggleSelection(bike.id) },
                onClick = { onBikeClick(bike) }
            ) {
                BikeItemRow(
                    bike = bike,
                    onClick = { onBikeClick(bike) }
                )
            }
        }
    }
}

@Composable
fun BikeItemRow(
    bike: Bike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRented = bike.rentalStart != null && bike.rentalEnd != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
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

                StatusBadge(isRented)
            }

            // 💰 Prix
            val formattedPrice = remember(bike.priceInCents) {
                val priceInEuros = bike.priceInCents / 100.0
                NumberFormat
                    .getCurrencyInstance(Locale.FRANCE)
                    .format(priceInEuros)
            }

            Text(
                text = formattedPrice,
                style = MaterialTheme.typography.bodyMedium
            )

            // 📅 Dates si location active
            if (isRented) {
                Text(
                    text = "→ ${formatDate(bike.rentalStart)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "← ${formatDate(bike.rentalEnd)}",
                    style = MaterialTheme.typography.bodySmall
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
        stringResource(R.string.renting)
    else
        stringResource(R.string.available)

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
fun BikeImage(bike: Bike) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(R.drawable.pic_bike),
            contentDescription = bike.title,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = bike.photoUrls.firstOrNull() ?: R.drawable.pic_bike,
            contentDescription = bike.title,
            modifier = Modifier
                .size(90.dp)
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
            onClick = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun BikeItemPreview() {
    LokaveloTheme {
        BikeItem(
            bikes = listOf(
                Bike(
                    id = "1",
                    title = "Origine Trail Explore",
                    priceInCents = 2500,
                    photoUrls = emptyList(),
                    rentalStart = Instant.parse("2026-02-21T16:30:00Z"),
                    rentalEnd = Instant.parse("2026-02-28T11:30:00Z")
                ),
                Bike(
                    id = "2",
                    title = "Origine Trail Explore",
                    priceInCents = 2500,
                    photoUrls = emptyList(),
                    rentalStart = null,
                    rentalEnd = null
                )
            ),
            onBikeClick = {},
            selectionState = SelectionState(),
            onToggleSelection = {}
        )
    }
}