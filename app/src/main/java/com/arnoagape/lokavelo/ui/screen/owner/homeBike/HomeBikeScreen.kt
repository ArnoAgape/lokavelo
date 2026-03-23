package com.arnoagape.lokavelo.ui.screen.owner.homeBike

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.ConfirmDeleteDialog
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.owner.rental.HomeRentalContent
import com.arnoagape.lokavelo.ui.screen.owner.rental.HomeRentalUiState
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeScreen(
    viewModel: HomeBikeViewModel,
    onAddBikeClick: () -> Unit,
    onBikeClick: (BikeWithRentals) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val rentalState by viewModel.rentalState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Synchronise le pager avec le tab sélectionné
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    // Synchronise le tab avec le swipe
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(pagerState.currentPage)
        if (pagerState.currentPage == 1) viewModel.markRentalsAsRead()
    }

    BackHandler(
        enabled = state.selection.isSelectionMode || state.isSearchActive
    ) {
        when {
            state.selection.isSelectionMode ->
                viewModel.exitSelectionMode()

            state.isSearchActive ->
                viewModel.toggleSearch()
        }
    }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            is Event.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                AnimatedContent(
                    targetState = state.isSearchActive,
                    transitionSpec = {
                        if (targetState) {
                            // Ouverture : slide depuis la droite + fade in
                            (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            // Fermeture : retour vers la droite
                            (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "topbar_search_transition"
                ) { isSearching ->
                    if (isSearching) {
                        SearchTopBar(
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onClose = viewModel::toggleSearch
                        )
                    } else {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = { Text(stringResource(R.string.rentals)) },
                            actions = {
                                if (selectedTab == 0) {
                                    if (!state.selection.isSelectionMode) {
                                        IconButton(onClick = viewModel::toggleSearch) {
                                            Icon(Icons.Default.Search, null)
                                        }
                                    }

                                    if (state.selection.isSelectionMode) {

                                        val hasSelection = state.selection.selectedIds.isNotEmpty()

                                        IconButton(
                                            onClick = {
                                                if (!state.selection.isSelectionMode) {
                                                    viewModel.enterSelectionMode()
                                                } else if (!hasSelection) {
                                                    viewModel.exitSelectionMode()
                                                } else {
                                                    viewModel.requestDeleteConfirmation()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector =
                                                    if (hasSelection)
                                                        Icons.Default.DeleteForever
                                                    else
                                                        Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.cd_button_delete_bike),
                                                tint =
                                                    if (hasSelection)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = viewModel::enterSelectionMode) {
                                            Icon(Icons.Default.Delete, null)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !state.isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                            text = { Text(stringResource(R.string.my_bikes)) }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                if (!state.isSearchActive) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                    viewModel.markRentalsAsRead()
                                }
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(stringResource(R.string.my_rentals))
                                    if (pendingCount > 0) {
                                        Badge {
                                            Text(pendingCount.toString())
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },

        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onAddBikeClick
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_bike))
                }
            }
        },

        ) { padding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !state.isSearchActive,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (page) {
                0 -> HomeBikeContent(
                    state = state,
                    onBikeClick = onBikeClick,
                    onRefresh = { viewModel.refreshBikes() },
                    onToggleSelection = { viewModel.toggleSelection(it) },
                    onEnterSelectionMode = { viewModel.enterSelectionMode() }
                )

                1 -> state.currentUser?.id?.let { userId ->
                    HomeRentalContent(
                        currentUserId = userId,
                        state = rentalState,
                        onRefresh = { viewModel.refreshRentals() },
                        onRentalClick = { TODO() },
                    )
                }
            }
        }

        val count = state.selection.selectedIds.size
        ConfirmDeleteDialog(
            show = showDeleteDialog,
            onConfirm = { viewModel.deleteSelectedBikes() },
            onDismiss = { viewModel.dismissDeleteDialog() },
            confirmButtonTitle = pluralStringResource(
                id = R.plurals.confirm_delete_bikes,
                count = count,
                count
            ),
            confirmButtonMessage = stringResource(R.string.delete_irreversible)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    onBikeClick: (BikeWithRentals) -> Unit,
    onRefresh: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onToggleSelection: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()

    when (val ui = state.bikesState) {

        is HomeBikeUiState.Success -> {
            PullToRefreshBox(
                modifier = modifier.fillMaxSize(),
                state = refreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh
            ) {
                BikeItem(
                    bikes = ui.bikes,
                    onBikeClick = onBikeClick,
                    selectionState = state.selection,
                    onToggleSelection = onToggleSelection,
                    onEnterSelectionMode = onEnterSelectionMode
                )
            }
        }

        is HomeBikeUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is HomeBikeUiState.Empty -> {
            ErrorOverlay(type = ErrorType.EMPTY_BIKES)
        }

        is HomeBikeUiState.SearchEmpty -> {
            ErrorOverlay(type = ErrorType.EMPTY_SEARCH)
        }

        is HomeBikeUiState.Error.Generic -> {
            ErrorOverlay(type = ErrorType.GENERIC)
        }
    }
}

@PreviewLightDark
@Composable
fun HomeBikeScreenPreview() {
    LokaveloTheme {

        val previewState = HomeState(
            bikesState = HomeBikeUiState.Success(PreviewData.bikesWithRentals),
            isRefreshing = false,
            rentalState = HomeRentalUiState.Empty,
            currentUser = null,
            selection = SelectionState(),
            searchQuery = "",
            isSearchActive = false
        )

        HomeBikeContent(
            state = previewState,
            onBikeClick = {},
            onRefresh = {},
            onToggleSelection = {},
            onEnterSelectionMode = {}
        )
    }
}