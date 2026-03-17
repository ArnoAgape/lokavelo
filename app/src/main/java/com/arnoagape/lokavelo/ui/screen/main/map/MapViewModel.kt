package com.arnoagape.lokavelo.ui.screen.main.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.GeocodingRepository
import com.arnoagape.lokavelo.data.repository.LocationRepository
import com.arnoagape.lokavelo.domain.model.AddressSuggestion
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeSize
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
    bikeRepository: BikeRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<MapEvent>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()
    private val _networkError = MutableStateFlow(false)

    private val bikesFlow: Flow<List<Bike>> =
        bikeRepository.observePublicBikes()
            .map { bikes ->
                Log.d("MAP", "All bikes size = ${bikes.size}")
                bikes.filter {
                    it.location.latitude != null &&
                            it.location.longitude != null
                }
            }

    val maxBikePrice: StateFlow<Float> =
        bikesFlow
            .map { bikes ->
                bikes.maxOfOrNull { it.priceInCents }
                    ?.let { it / 100f }
                    ?: 100f
            }
            .onEach { maxPrice ->
                if (_filters.value.maxPrice == 100f) {
                    _filters.value = _filters.value.copy(maxPrice = maxPrice)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                200f
            )

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
                            filters.startDate,
                            filters.endDate
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
            combine(filteredBikes, selectedDays, _filters) { bikes, days, filters ->
                Triple(bikes, days, filters)
            },
            combine(suggestions, maxBikePrice, _networkError) { suggestions, maxPrice, networkError ->
                Triple(suggestions, maxPrice, networkError)
            }
        ) { (bikes, days, filters), (suggestions, maxPrice, networkError) ->
            HomeScreenState(
                filteredBikes = bikes,
                selectedDays = days,
                filters = filters,
                suggestions = suggestions,
                maxBikePrice = maxPrice,
                networkError = networkError
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeScreenState()
        )

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _userLocation.value = locationRepository.getLastLocation()
        }
    }

    fun onBikeCardClicked(): Boolean {
        return if (!networkUtils.isNetworkAvailable()) {
            _networkError.value = true
            false
        } else {
            true
        }
    }

    fun clearNetworkError() {
        _networkError.value = false
    }

    fun updateAddressFromSuggestion(suggestion: AddressSuggestion) {
        _filters.value = _filters.value.copy(
            addressQuery = null,
            center = GeoPoint(
                suggestion.lat,
                suggestion.lon
            ),
            maxDistanceKm = null
        )

        _addressQuery.value = ""
    }

    fun updateDates(start: LocalDate, end: LocalDate) {
        _filters.value = _filters.value.copy(
            startDate = start,
            endDate = end
        )
    }

    fun updateBikeCategory(categories: Set<BikeCategory>) {
        _filters.value = _filters.value.copy(
            bikeCategories = categories
        )
    }

    fun updateElectricFilter(isElectric: Boolean?) {
        _filters.value = _filters.value.copy(
            electricOnly = isElectric
        )
    }

    fun updateFilters(
        bikeSizes: Set<BikeSize>,
        accessories: Set<BikeEquipment>,
        minPrice: Float,
        maxPrice: Float
    ) {
        _filters.value = _filters.value.copy(
            bikeSizes = bikeSizes,
            accessories = accessories,
            minPrice = minPrice,
            maxPrice = maxPrice
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
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val bikeCategories: Set<BikeCategory> = emptySet(),
    val electricOnly: Boolean? = null,
    val bikeSizes: Set<BikeSize> = emptySet(),
    val accessories: Set<BikeEquipment> = emptySet(),
    val minPrice: Float = 0f,
    val maxPrice: Float = 100f,
)

data class HomeScreenState(
    val filters: SearchFilters = SearchFilters(),
    val filteredBikes: List<Bike> = emptyList(),
    val selectedDays: Long = 1L,
    val suggestions: List<AddressSuggestion> = emptyList(),
    val maxBikePrice: Float = 100f,
    val networkError: Boolean = false
)