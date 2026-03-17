package com.arnoagape.lokavelo.ui.screen.owner.rental

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.priority
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.BikeItemRow
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.HomeRentalScreenState
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.RentalStatusBadge
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.RentalWithBike
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRentalContent(
    modifier: Modifier = Modifier,
    state: HomeRentalScreenState,
    onRefresh: () -> Unit = {},
    onRentalClick: (Rental) -> Unit = {}
) {

    val refreshState = rememberPullToRefreshState()

    when (val ui = state.uiState) {

        is HomeRentalUiState.Success -> {
            PullToRefreshBox(
                modifier = modifier.fillMaxSize(),
                state = refreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh
            ) {
                val allRentals = (ui.pending + ui.active + ui.history)
                    .sortedBy { it.rental.status.priority() }

                val activeRentals = allRentals.filter {
                    it.rental.status != RentalStatus.COMPLETED &&
                            it.rental.status != RentalStatus.DECLINED &&
                            it.rental.status != RentalStatus.CANCELLED
                }

                val historyRentals = allRentals - activeRentals.toSet()

                var showHistory by remember { mutableStateOf(false) }

                LazyColumn(
                    contentPadding = PaddingValues(top = 10.dp)
                ) {

                    items(activeRentals, key = { it.rental.id }) {
                        RentalItem(it) { onRentalClick(it.rental) }
                    }

                    if (historyRentals.isNotEmpty()) {
                        item {
                            TextButton(onClick = { showHistory = !showHistory }) {
                                Text(
                                    if (showHistory) "Masquer l’historique"
                                    else "Afficher l’historique"
                                )
                            }
                        }
                    }

                    if (showHistory) {
                        items(historyRentals, key = { it.rental.id }) {
                            RentalItem(it) { onRentalClick(it.rental) }
                        }
                    }
                }
            }
        }

        HomeRentalUiState.Loading -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        HomeRentalUiState.Empty -> {

            ErrorOverlay(type = ErrorType.EMPTY_RENTALS)
        }

        HomeRentalUiState.Error.Generic -> {

            ErrorOverlay(type = ErrorType.GENERIC)
        }
    }
}

@Composable
fun RentalItem(
    rentalWithBike: RentalWithBike,
    onClick: () -> Unit
) {
    val rental = rentalWithBike.rental
    val bike = rentalWithBike.bike

    SelectItemRow(
        id = rental.id,
        isSelectionMode = false,
        isSelected = false,
        onSelectToggle = {},
        onClick = onClick,
        onLongClick = {}
    ) {
        BikeItemRow(
            bike = bike,
            startDate = rental.startDate
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            endDate = rental.endDate
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            badge = {
                RentalStatusBadge(rental.status)
            }
        )
    }
}

@PreviewLightDark
@Composable
fun HomeRentalPreview() {

    val bike = Bike(
        id = "bike1",
        title = "Origine Trail",
        priceInCents = 2500,
        photoUrls = emptyList()
    )

    val rentals = listOf(
        RentalWithBike(
            rental = Rental(
                id = "1",
                bikeId = "bike1",
                ownerId = "owner",
                renterId = "user1",
                startDate = Instant.now(),
                endDate = Instant.now().plusSeconds(86400 * 3),
                priceTotalInCents = 7500,
                status = RentalStatus.ACCEPTED
            ),
            bike = bike
        )
    )

    LokaveloTheme {
        HomeRentalContent(
            state = HomeRentalScreenState(
                uiState = HomeRentalUiState.Success(
                    pending = rentals,
                    active = emptyList(),
                    history = emptyList()
                )
            )
        )
    }
}