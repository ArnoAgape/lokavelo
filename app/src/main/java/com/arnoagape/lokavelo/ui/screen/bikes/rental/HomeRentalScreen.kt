package com.arnoagape.lokavelo.ui.screen.bikes.rental

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.RentalWithBike
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.SelectItemRow
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.BikeItemContext
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.BikeItemRow
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.RentalStatusBadge
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeBikeViewModel
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeRentalScreenState
import kotlinx.coroutines.launch
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRentalScreen(
    viewModel: HomeBikeViewModel
) {
    val rentalTabsState by viewModel.rentalTabsState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(stringResource(R.string.rentals)) }
                )

                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {

                    // PROPRIÉTAIRE
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            viewModel.markRentalsAsRead()
                        },
                        text = {
                            BadgedBox(
                                badge = {
                                    if (pendingCount > 0) {
                                        Badge { Text(pendingCount.toString()) }
                                    }
                                }
                            ) {
                                Text(
                                    stringResource(R.string.owner),
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                            }
                        }
                    )

                    // LOCATAIRE
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = { Text(stringResource(R.string.tenant)) }
                    )
                }
            }
        }
    ) { padding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->

            when (page) {

                // OWNER TAB
                0 -> OwnerRentalContent(
                    state = rentalTabsState.owner,
                    onRefresh = { viewModel.refreshRentals() }
                )

                // RENTER TAB
                1 -> {
                    currentUser?.id?.let {
                        RenterRentalContent(
                            state = rentalTabsState.renter,
                            onRefresh = { viewModel.refreshRentals() },
                            onRentalClick = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RentalItem(
    rentalWithBike: RentalWithBike,
    currentUserId: String,
    onClick: () -> Unit
) {
    val rental = rentalWithBike.rental
    val bike = rentalWithBike.bike
    val isOwner = rental.ownerId == currentUserId

    val startDate = rental.startDate.atZone(ZoneId.systemDefault()).toLocalDate()
    val endDate = rental.endDate.atZone(ZoneId.systemDefault()).toLocalDate()

    val context = if (isOwner) {
        BikeItemContext.OwnerRental(
            bike = bike,
            rental = rental,
            startDate = startDate,
            endDate = endDate
        )
    } else {
        BikeItemContext.RenterRental(
            bike = bike,
            rental = rental,
            startDate = startDate,
            endDate = endDate
        )
    }

    SelectItemRow(
        isSelectionMode = false,
        isSelected = false,
        onSelectToggle = {},
        onClick = onClick,
        onLongClick = {},
        badge = {
            when (context) {

                is BikeItemContext.OwnerRental -> {
                    RentalStatusBadge(context.rental.status)
                }

                is BikeItemContext.RenterRental -> {
                    RentalStatusBadge(context.rental.status)
                }

                else -> Unit
            }
        }
    ) {
        BikeItemRow(context = context)
    }
}

@PreviewLightDark
@Composable
fun RenterRentalPreview() {
    LokaveloTheme {
        RenterRentalContent(
            state = HomeRentalScreenState(
                uiState = HomeRentalUiState.Success(
                    pending = PreviewData.rentalsWithBike,
                    active = emptyList(),
                    history = emptyList()
                )
            )
        )
    }
}