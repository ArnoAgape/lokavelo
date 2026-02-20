package com.arnoagape.lokavelo.ui.screen.owner.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.navigation.EmbeddedSearchBar
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.ConfirmDeleteDialog
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeScreen(
    viewModel: HomeBikeViewModel,
    onAddBikeClick: () -> Unit,
    onBikeClick: (Bike) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

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

        topBar = {

            if (state.isSearchActive) {
                EmbeddedSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClose = viewModel::toggleSearch,
                    modifier = Modifier
                        .padding(top = 42.dp, start = 16.dp, end = 16.dp)
                        .focusRequester(focusRequester)
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.rentals)) },
                    actions = {

                        if (!state.selection.isSelectionMode) {
                            IconButton(onClick = viewModel::toggleSearch) {
                                Icon(Icons.Default.Search, null)
                            }
                        }

                        if (state.selection.isSelectionMode) {
                            IconButton(
                                onClick = {
                                    if (state.selection.selectedIds.isEmpty())
                                        viewModel.exitSelectionMode()
                                    else
                                        viewModel.requestDeleteConfirmation()
                                }
                            ) {
                                Icon(
                                    if (state.selection.selectedIds.isEmpty())
                                        Icons.Default.Close
                                    else
                                        Icons.Default.DeleteForever,
                                    null
                                )
                            }
                        } else {
                            IconButton(onClick = viewModel::enterSelectionMode) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                )
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onAddBikeClick
            ) {
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.add_bike))
            }
        }

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
            confirmButtonTitle = stringResource(R.string.confirm_delete_bike),
            confirmButtonMessage = stringResource(R.string.confirm_delete_message_bikes)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeContent(
    modifier: Modifier = Modifier,
    state: HomeBikeScreenState,
    onBikeClick: (Bike) -> Unit,
    onRefresh: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onToggleSelection: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()

    when (val ui = state.uiState) {

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
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_bike),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
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

        val sampleBikes = listOf(
            Bike(id = "1", title = "Vélo gravel Origine", priceInCents = 2500),
            Bike(id = "2", title = "Vélo VTT Rockrider", priceInCents = 1000),
            Bike(id = "3", title = "Vélo randonneuse Riverside", priceInCents = 2000)
        )

        val previewState = HomeBikeScreenState(
            uiState = HomeBikeUiState.Success(sampleBikes),
            isRefreshing = false
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