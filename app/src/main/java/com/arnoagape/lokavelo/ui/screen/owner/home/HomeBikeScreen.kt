package com.arnoagape.lokavelo.ui.screen.owner.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@Composable
fun HomeBikeScreen(
    viewModel: HomeBikeViewModel,
    onBikeClick: (Bike) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current

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

    HomeBikeContent(
        state = state,
        onBikeClick = onBikeClick,
        onRefresh = { viewModel.refreshMedicines() },
        onToggleSelection = { viewModel.toggleSelection(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBikeContent(
    state: HomeBikeScreenState,
    onBikeClick: (Bike) -> Unit,
    onRefresh: () -> Unit,
    onToggleSelection: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh
    ) {
        when (val ui = state.uiState) {

            is HomeBikeUiState.Success -> {
                BikeItem(
                    bikes = ui.bikes,
                    onBikeClick = onBikeClick,
                    selectionState = state.selection,
                    onToggleSelection = onToggleSelection
                )
            }

            is HomeBikeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {}
        }
    }
}

@PreviewLightDark
@Composable
fun HomeBikeScreenPreview() {
    LokaveloTheme {

        val sampleBikes = listOf(
            Bike(title = "Vélo gravel Origine", priceInCents = 2500),
            Bike(title = "Vélo VTT Rockrider", priceInCents = 1000),
            Bike(title = "Vélo randonneuse Riverside", priceInCents = 2000)
        )

        val previewState = HomeBikeScreenState(
            uiState = HomeBikeUiState.Success(sampleBikes),
            isRefreshing = false
        )

        HomeBikeContent(
            state = previewState,
            onBikeClick = {},
            onRefresh = {},
            onToggleSelection = {}
        )
    }
}