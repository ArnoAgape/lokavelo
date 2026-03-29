package com.arnoagape.lokavelo.ui.screen.main.map.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    val pagerState = rememberPagerState(pageCount = { bikes.size })

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 26.dp),
        pageSpacing = 12.dp
    ) { page ->
        BikePreviewCard(
            bike = bikes[page],
            filters = filters,
            onBikeClick = onBikeClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}