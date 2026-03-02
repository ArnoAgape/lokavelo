package com.arnoagape.lokavelo.ui.screen.main.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.data.repository.GeocodingRepository
import com.arnoagape.lokavelo.data.repository.LocationRepository
import com.arnoagape.lokavelo.domain.model.AddressSuggestion
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.isBikeMatchingFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
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

    private val _filters = MutableStateFlow(SearchFilters())

    private val _addressQuery = MutableStateFlow("")
    fun onAddressQueryChange(query: String) {
        _addressQuery.value = query
    }

    val suggestions: StateFlow<List<AddressSuggestion>> =
        _addressQuery
            .debounce(400)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.length < 3) {
                    flowOf(emptyList())
                } else {
                    flowOf(geocodingRepository.search(query))
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val selectedDays: StateFlow<Long> =
        _filters
            .map { filters ->
                if (filters.startDate != null && filters.endDate != null) {
                    maxOf(
                        1,
                        ChronoUnit.DAYS.between(
                            filters.startDate.toLocalDate(),
                            filters.endDate.toLocalDate()
                        )
                    )
                } else {
                    1
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                1
            )

    private val filteredBikes: StateFlow<List<Bike>> =
        combine(bikesFlow, _filters) { bikes, filters ->
            bikes.filter { isBikeMatchingFilters(it, filters) }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val state: StateFlow<HomeScreenState> =
        combine(
            filteredBikes,
            selectedDays,
            _filters,
            suggestions
        ) { bikes, days, filters, suggestions ->
            HomeScreenState(
                filteredBikes = bikes,
                selectedDays = days,
                filters = filters,
                suggestions = suggestions
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeScreenState()
        )

    fun updateAddressFromSuggestion(suggestion: AddressSuggestion) {
        _filters.value = _filters.value.copy(
            addressQuery = null,   // important
            center = GeoPoint(
                suggestion.lat,
                suggestion.lon
            ),
            maxDistanceKm = null
        )

        _addressQuery.value = ""
    }

    fun updateDates(start: LocalDateTime?, end: LocalDateTime?) {
        _filters.value = _filters.value.copy(
            startDate = start,
            endDate = end
        )
    }

    fun clearLocationFilter() {
        _filters.value = _filters.value.copy(
            center = null,
            addressQuery = null
        )
    }
}

data class SearchFilters(
    val addressQuery: String? = null,
    val center: GeoPoint? = null,
    val maxDistanceKm: Double? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class HomeScreenState(
    val filters: SearchFilters = SearchFilters(),
    val filteredBikes: List<Bike> = emptyList(),
    val selectedDays: Long = 1L,
    val suggestions: List<AddressSuggestion> = emptyList()
)