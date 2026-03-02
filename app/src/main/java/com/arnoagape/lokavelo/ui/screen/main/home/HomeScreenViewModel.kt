package com.arnoagape.lokavelo.ui.screen.main.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.data.repository.LocationRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.normalizeForSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    bikeRepository: BikeOwnerRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    val userLocation: StateFlow<Location?> =
        flow {
            emit(locationRepository.getLastLocation())
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val bikesFlow: Flow<List<Bike>> = bikeRepository.observeBikes()

    private val _searchQuery = MutableStateFlow("")

    private val _isSearchActive = MutableStateFlow(false)

    private val uiState: Flow<HomeScreenUiState> =
        combine(bikesFlow, _searchQuery) { bikes, query ->

            val filtered = if (query.isBlank()) {
                bikes
            } else {
                val normalized = query.normalizeForSearch()
                bikes.filter {
                    it.title.normalizeForSearch().contains(normalized)
                }
            }

            when {
                filtered.isEmpty() -> HomeScreenUiState.Empty
                else -> HomeScreenUiState.Success(filtered)
            }
        }
            .onStart { emit(HomeScreenUiState.Loading) }
            .catch { emit(HomeScreenUiState.Error.Generic()) }

    val state: StateFlow<HomeScreenState> =
        combine(
            uiState,
            _searchQuery,
            _isSearchActive
        ) { ui, query, active ->
            HomeScreenState(
                uiState = ui,
                searchQuery = query,
                isSearchActive = active
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeScreenState()
        )
}

data class HomeScreenState(
    val uiState: HomeScreenUiState = HomeScreenUiState.Loading,
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)