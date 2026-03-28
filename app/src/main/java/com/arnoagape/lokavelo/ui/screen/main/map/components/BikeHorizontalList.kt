package com.arnoagape.lokavelo.ui.screen.main.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters

@Composable
fun BikeHorizontalList(
    bikes: List<Bike>,
    filters: SearchFilters,
    onBikeClick: (Bike) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bikes, key = { it.id }) { bike ->
            BikePreviewCard(
                bike = bike,
                filters = filters,
                onBikeClick = onBikeClick,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}