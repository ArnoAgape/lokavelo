package com.arnoagape.lokavelo.ui.screen.owner.homeBike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.RentalRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.User
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.screen.rental.HomeRentalUiState
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.normalizeForSearch
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for the owner home bike screen.
 * Handles bike listing and user actions related to a bike.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeBikeViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val userRepository: UserRepository,
    rentalRepository: RentalRepository,
    auth: FirebaseAuth,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val currentUserId = auth.currentUser?.uid

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val bikesFlow: Flow<List<BikeWithRentals>> = bikeRepository.observeOwnerBikesWithRentals()

    val currentUser: StateFlow<User?> =
        userRepository.observeCurrentUser()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val rentalsFlow: Flow<List<RentalWithBike>> =
        rentalRepository.observeAllMyRentals()
            .flatMapLatest { rentals ->

                val bikeIds = rentals.map { it.bikeId }.distinct()

                bikeRepository.observeBikesByIds(bikeIds)
                    .map { bikes ->

                        val bikeMap = bikes.associateBy { it.id }

                        rentals.mapNotNull { rental ->
                            bikeMap[rental.bikeId]?.let { bike ->
                                RentalWithBike(rental, bike)
                            }
                        }
                    }
            }

    private val _selection = MutableStateFlow(SelectionState())
    private val _isRefreshing = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _selectedTab = MutableStateFlow(0) // 0 = Mes vélos, 1 = Mes locations
    val selectedTab: StateFlow<Int> = _selectedTab
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val rentalUiState: Flow<HomeRentalUiState> =
        rentalsFlow
            .map { rentals ->

                val (pending, others) = rentals.partition {
                    it.rental.status == RentalStatus.PENDING
                }

                val (active, history) = others.partition {
                    it.rental.status == RentalStatus.ACCEPTED ||
                            it.rental.status == RentalStatus.ACTIVE
                }

                if (rentals.isEmpty()) {
                    HomeRentalUiState.Empty
                } else {
                    HomeRentalUiState.Success(
                        pending = pending,
                        active = active,
                        history = history
                    )
                }
            }
            .onStart { emit(HomeRentalUiState.Loading) }
            .catch { emit(HomeRentalUiState.Error.Generic) }

    val rentalState: StateFlow<HomeRentalScreenState> =
        combine(
            rentalUiState,
            _isRefreshing
        ) { ui, refreshing ->
            HomeRentalScreenState(
                uiState = ui,
                isRefreshing = refreshing
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeRentalScreenState()
        )

    private val uiState: Flow<HomeBikeUiState> =
        combine(bikesFlow, _searchQuery) { bikes, query ->

            val normalizedQuery = query.normalizeForSearch()

            val filtered = if (query.isBlank()) {
                bikes
            } else {
                bikes.filter {
                    it.bike.title.normalizeForSearch()
                        .contains(normalizedQuery)
                }
            }
            if (filtered.isEmpty()) {
                HomeBikeUiState.SearchEmpty
            } else {
                HomeBikeUiState.Success(filtered)
            }
        }
            .onStart {
                emit(HomeBikeUiState.Loading)
            }
            .catch { e ->
                Log.e("HOME_BIKE_ERROR", "🔥 Error loading bikes", e)
                emit(HomeBikeUiState.Error.Generic())
            }

    private val dataState: Flow<Triple<HomeBikeUiState, HomeRentalUiState, User?>> =
        combine(
            uiState,
            rentalUiState,
            currentUser
        ) { bikes, rentals, user ->
            Triple(bikes, rentals, user)
        }

    val state: StateFlow<HomeState> =
        combine(
            dataState,
            _isRefreshing,
            _selection,
            _searchQuery,
            _isSearchActive
        ) { (bikes, rentals, user), refreshing, selection, query, active ->

            HomeState(
                bikesState = bikes,
                rentalState = rentals,
                currentUser = user,
                isRefreshing = refreshing,
                selection = selection,
                searchQuery = query,
                isSearchActive = active
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                HomeState(
                    bikesState = HomeBikeUiState.Loading,
                    rentalState = HomeRentalUiState.Loading,
                    currentUser = null,
                    isRefreshing = false,
                    selection = SelectionState(),
                    searchQuery = "",
                    isSearchActive = false
                )
            )

    val pendingCount: StateFlow<Int> =
        if (currentUserId == null) {
            flowOf(0).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )
        } else {
            userRepository
                .observePendingRentalsUnread(currentUserId)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    0
                )
        }

    // Resets badge
    fun markRentalsAsRead() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            userRepository.markRentalsAsRead(userId)
        }
    }

    // Rent/Owner Tab
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // 🔍 Search

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.update { !it }

        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    // 🗑 Selection

    fun requestDeleteConfirmation() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun enterSelectionMode() {
        _selection.update { it.copy(isSelectionMode = true) }
    }

    fun exitSelectionMode() {
        _selection.value = SelectionState()
    }

    fun toggleSelection(id: String) {
        _selection.update { sel ->
            val set = sel.selectedIds.toMutableSet()

            if (!set.add(id)) set.remove(id)

            if (set.isEmpty()) {
                SelectionState()
            } else {
                sel.copy(selectedIds = set)
            }
        }
    }

    fun deleteSelectedBikes() {
        viewModelScope.launch {
            val result = bikeRepository.deleteBikes(_selection.value.selectedIds)

            if (result.isSuccess) {
                exitSelectionMode()
                _events.trySend(Event.ShowSuccessMessage(R.string.success_bike_deleted))
            } else {
                _events.trySend(Event.ShowMessage(R.string.error_delete_bike))
            }

            _showDeleteDialog.value = false
        }
    }

    private fun refresh(block: suspend () -> Unit = {}) {
        if (!networkUtils.isNetworkAvailable()) {
            _events.trySend(Event.ShowMessage(R.string.error_no_network))
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            block()
            delay(700)
            _isRefreshing.value = false
        }
    }

    fun refreshBikes() = refresh()
    fun refreshRentals() = refresh()
}

data class HomeState(
    val bikesState: HomeBikeUiState,
    val rentalState: HomeRentalUiState,
    val currentUser: User?,
    val isRefreshing: Boolean,
    val selection: SelectionState,
    val searchQuery: String,
    val isSearchActive: Boolean
)

data class HomeRentalScreenState(
    val uiState: HomeRentalUiState = HomeRentalUiState.Loading,
    val isRefreshing: Boolean = false
)

data class RentalWithBike(
    val rental: Rental,
    val bike: Bike
)