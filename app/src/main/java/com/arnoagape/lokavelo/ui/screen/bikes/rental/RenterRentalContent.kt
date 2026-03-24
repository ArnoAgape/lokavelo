package com.arnoagape.lokavelo.ui.screen.bikes.rental

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeRentalScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterRentalContent(
    modifier: Modifier = Modifier,
    state: HomeRentalScreenState,
    onRefresh: () -> Unit = {},
    onRentalClick: (Rental) -> Unit = {}
) {

    val refreshState = rememberPullToRefreshState()

    when (val ui = state.uiState) {

        is HomeRentalUiState.Success -> {

            val activeRentals = ui.pending + ui.active
            val historyRentals = ui.history

            var showHistory by remember { mutableStateOf(false) }

            PullToRefreshBox(
                modifier = modifier.fillMaxSize(),
                state = refreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh
            ) {

                LazyColumn(
                    contentPadding = PaddingValues(top = 10.dp)
                ) {

                    // 🔵 ACTIVE + PENDING
                    items(activeRentals, key = { it.rental.id }) {
                        RentalItem(
                            rentalWithBike = it,
                            currentUserId = it.rental.renterId, // ou supprime si inutile
                        ) { onRentalClick(it.rental) }
                    }

                    // 📜 HISTORY TOGGLE
                    if (historyRentals.isNotEmpty()) {
                        item {
                            HistoryToggle(
                                showHistory = showHistory,
                                onToggle = { showHistory = !showHistory }
                            )
                        }
                    }

                    // 📦 HISTORY LIST
                    if (showHistory) {
                        items(historyRentals, key = { it.rental.id }) {
                            RentalItem(
                                rentalWithBike = it,
                                currentUserId = it.rental.renterId
                            ) { onRentalClick(it.rental) }
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

@Composable
private fun HistoryToggle(
    showHistory: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (showHistory)
                stringResource(R.string.hide_history)
            else
                stringResource(R.string.show_history),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(6.dp))

        Icon(
            imageVector = if (showHistory)
                Icons.Default.KeyboardArrowUp
            else
                Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}