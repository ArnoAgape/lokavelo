package com.arnoagape.lokavelo.ui.screen.owner.homeBike

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeScreen(
    viewModel: HomeBikeViewModel,
    onAddBikeClick: () -> Unit,
    onBikeClick: (Bike) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val rentalState by viewModel.rentalState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

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
                if (state.isSearchActive) {
                    EmbeddedSearchBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClose = viewModel::toggleSearch,
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .focusRequester(focusRequester)
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
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text(stringResource(R.string.my_bikes)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            viewModel.selectTab(1)
                            viewModel.markRentalsAsRead()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Mes locations")
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
        when (selectedTab) {
            0 -> HomeBikeContent(
                modifier = Modifier.padding(padding),
                state = state,
                onBikeClick = onBikeClick,
                onRefresh = { viewModel.refreshBikes() },
                onToggleSelection = { viewModel.toggleSelection(it) },
                onEnterSelectionMode = { viewModel.enterSelectionMode() }
            )

            1 ->
                state.currentUser?.id?.let { userId ->
                    HomeRentalContent(
                        modifier = Modifier.padding(padding),
                        currentUserId = userId,
                        state = rentalState,
                        onRefresh = { viewModel.refreshRentals() },
                        onRentalClick = { TODO() },
                    )
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
    onBikeClick: (Bike) -> Unit,
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

        is HomeBikeUiState.Error.Generic -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_loading_bike),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun HomeBikeScreenPreview() {
    LokaveloTheme {

        val previewState = HomeState(
            bikesState = HomeBikeUiState.Success(PreviewData.bikes),
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