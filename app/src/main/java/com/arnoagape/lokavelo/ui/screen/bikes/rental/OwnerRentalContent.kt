package com.arnoagape.lokavelo.ui.screen.bikes.rental

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.SelectItemRow
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.bikes.BikeItemContext
import com.arnoagape.lokavelo.ui.screen.bikes.BikeItemRow
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeRentalScreenState
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRentalContent(
    state: HomeRentalScreenState,
    onRefresh: () -> Unit = {},
    onRentalClick: (Rental) -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()

    when (val ui = state.uiState) {

        is HomeRentalUiState.Success -> {

            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                state = refreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh
            ) {

                val allRentals = ui.pending + ui.active + ui.history

                LazyColumn(
                    contentPadding = PaddingValues(top = 10.dp)
                ) {

                    items(allRentals, key = { it.rental.id }) { item ->

                        val rental = item.rental

                        val startDate = rental.startDate
                            .atZone(ZoneId.systemDefault()).toLocalDate()

                        val endDate = rental.endDate
                            .atZone(ZoneId.systemDefault()).toLocalDate()

                        val context = BikeItemContext.OwnerRental(
                            bike = item.bike,
                            rental = rental,
                            startDate = startDate,
                            endDate = endDate
                        )

                        SelectItemRow(
                            id = rental.id,
                            isSelectionMode = false,
                            isSelected = false,
                            onSelectToggle = {},
                            onClick = { onRentalClick(rental) },
                            onLongClick = {}
                        ) {
                            BikeItemRow(context = context)
                        }
                    }
                }
            }
        }

        HomeRentalUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

@PreviewLightDark
@Composable
fun OwnerRentalContentPreview() {
    LokaveloTheme {

        val rentals = PreviewData.rentalsWithBike

        OwnerRentalContent(
            state = HomeRentalScreenState(
                uiState = HomeRentalUiState.Success(
                    pending = rentals,
                    active = emptyList(),
                    history = emptyList()
                ),
                isRefreshing = false
            )
        )
    }
}