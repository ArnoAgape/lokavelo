package com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.common.components.ConfirmDeleteDialog
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.SearchTopBar
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.BikeItem
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.BikeItemContext
import com.arnoagape.lokavelo.ui.screen.bikes.rental.HomeRentalUiState
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeScreen(
    viewModel: HomeBikeViewModel,
    onAddBikeClick: () -> Unit,
    onBikeClick: (BikeWithRentals) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current

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
                            navigationIcon = {
                                if (state.selection.isSelectionMode) {
                                    IconButton(onClick = viewModel::exitSelectionMode) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            },
                            title = {
                                when {
                                    state.selection.isSelectionMode -> {
                                        val count = state.selection.selectedIds.size
                                        Text(
                                            if (count == 0) stringResource(R.string.select_bikes)
                                            else pluralStringResource(R.plurals.bikes_selected, count, count)
                                        )
                                    }
                                    else -> Text(stringResource(R.string.garage))
                                }
                            },
                            actions = {
                                when {
                                    state.selection.isSelectionMode -> {
                                        val hasSelection = state.selection.selectedIds.isNotEmpty()

                                        if (hasSelection) {
                                            val selectedBikes = (state.bikesState as? HomeBikeUiState.Success)
                                                ?.bikes
                                                ?.filter { it.bike.id in state.selection.selectedIds }
                                                ?: emptyList()

                                            val allAvailable = selectedBikes.all { it.bike.available }
                                            val allUnavailable = selectedBikes.all { !it.bike.available }
                                            val isMixed = !allAvailable && !allUnavailable

                                            var menuExpanded by remember { mutableStateOf(false) }

                                            IconButton(onClick = viewModel::requestDeleteConfirmation) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteForever,
                                                    contentDescription = stringResource(R.string.cd_button_delete_bike),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                            
                                            Box {
                                                IconButton(onClick = { menuExpanded = true }) {
                                                    Icon(Icons.Default.MoreVert, null)
                                                }
                                                DropdownMenu(
                                                    expanded = menuExpanded,
                                                    onDismissRequest = { menuExpanded = false }
                                                ) {
                                                    if (allUnavailable || isMixed) {
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.make_available)) },
                                                            onClick = {
                                                                viewModel.setAvailability(true)
                                                                menuExpanded = false
                                                            }
                                                        )
                                                    }
                                                    if (allAvailable || isMixed) {
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.make_unavailable)) },
                                                            onClick = {
                                                                viewModel.setAvailability(false)
                                                                menuExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        IconButton(onClick = viewModel::toggleSearch) {
                                            Icon(Icons.Default.Search, null)
                                        }
                                        IconButton(onClick = viewModel::enterSelectionMode) {
                                            Icon(Icons.Default.Delete, null)
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
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onAddBikeClick
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.add_bike))
            }
        },

        ) { padding ->

        HomeBikeContent(
            modifier = Modifier.padding(padding),
            state = state,
            onBikeClick = onBikeClick,
            onRefresh = { viewModel.refreshBikes() },
            onToggleSelection = { viewModel.toggleSelection(it) },
            onEnterSelectionMode = { viewModel.enterSelectionMode() }
        )

        ConfirmDeleteDialog(
            show = showDeleteDialog,
            onConfirm = { viewModel.deleteSelectedBikes() },
            onDismiss = { viewModel.dismissDeleteDialog() },
            confirmButtonTitle = pluralStringResource(
                id = R.plurals.confirm_delete_bikes,
                count = state.selection.selectedIds.size,
                state.selection.selectedIds.size
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
                    onEnterSelectionMode = onEnterSelectionMode,
                    contextBuilder = { bikeWithRentals ->
                        BikeItemContext.OwnerGarage(bike = bikeWithRentals.bike)
                    }
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