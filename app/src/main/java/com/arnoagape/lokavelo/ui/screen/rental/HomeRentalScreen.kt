package com.arnoagape.lokavelo.ui.screen.rental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.BikeItemRow
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.HomeBikeUiState
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.HomeBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.homeBike.StatusDotWithTooltip
import com.arnoagape.lokavelo.ui.utils.mainRental
import com.arnoagape.lokavelo.ui.utils.toDisplayStatus
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRentalScreen(
    viewModel: HomeBikeViewModel
) {
    val rentalState by viewModel.rentalState.collectAsStateWithLifecycle()
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
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = { Text(stringResource(R.string.owner)) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            viewModel.markRentalsAsRead()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(stringResource(R.string.tenant))
                                if (pendingCount > 0) {
                                    Badge { Text(pendingCount.toString()) }
                                }
                            }
                        }
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
                0 -> OwnerRentalContent(
                    viewModel = viewModel
                )
                1 -> currentUser?.id?.let { userId ->
                    HomeRentalContent(
                        currentUserId = userId,
                        state = rentalState,
                        onRefresh = { viewModel.refreshRentals() },
                        onRentalClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRentalContent(
    viewModel: HomeBikeViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()

    when (val ui = state.bikesState) {
        is HomeBikeUiState.Success -> {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                state = refreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refreshBikes() }
            ) {
                LazyColumn(contentPadding = PaddingValues(top = 10.dp)) {
                    items(
                        items = ui.bikes,
                        key = { it.bike.id }
                    ) { item ->
                        val mainRental = item.rentals.mainRental()
                        val today = LocalDate.now()
                        val start = mainRental?.startDate
                            ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        val end = mainRental?.endDate
                            ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        val rentalStatus = mainRental?.toDisplayStatus(today)

                        BikeItemRow(
                            bike = item.bike,
                            startDate = start,
                            endDate = end,
                            badge = { StatusDotWithTooltip(rentalStatus = rentalStatus) }
                        )
                    }
                }
            }
        }
        is HomeBikeUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeBikeUiState.Empty -> ErrorOverlay(type = ErrorType.EMPTY_BIKES)
        else -> ErrorOverlay(type = ErrorType.GENERIC)
    }
}