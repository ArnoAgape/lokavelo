package com.arnoagape.lokavelo.ui.screen.owner.home

import  androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.SelectionState
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.normalizeForSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
@HiltViewModel
class HomeBikeViewModel @Inject constructor(
    private val bikeRepository: BikeOwnerRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _selection = MutableStateFlow(SelectionState())
    private val _isRefreshing = MutableStateFlow(false)

    private val bikesFlow: Flow<List<Bike>> = bikeRepository.observeBikes()

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val uiState: Flow<HomeBikeUiState> =
        combine(bikesFlow, _searchQuery) { bikes, query ->

            val normalizedQuery = query.normalizeForSearch()

            val filtered = if (query.isBlank()) {
                bikes
            } else {
                bikes.filter {
                    it.title.normalizeForSearch()
                        .contains(normalizedQuery)
                }
            }
            if (filtered.isEmpty()) {
                HomeBikeUiState.Empty()
            } else {
                HomeBikeUiState.Success(filtered)
            }
        }
            .onStart {
                emit(HomeBikeUiState.Loading)
            }
            .catch {
                emit(HomeBikeUiState.Error.Generic())
            }

    val state: StateFlow<HomeBikeScreenState> =
        combine(
            uiState,
            _isRefreshing,
            _selection,
            _searchQuery,
            _isSearchActive
        ) { ui, refreshing, selection, query, active ->
            HomeBikeScreenState(
                uiState = ui,
                isRefreshing = refreshing,
                selection = selection,
                searchQuery = query,
                isSearchActive = active
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeBikeScreenState()
        )

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
            sel.copy(selectedIds = set)
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

    // 🔄 Refresh

    fun refreshBikes() {
        if (!networkUtils.isNetworkAvailable()) {
            _events.trySend(Event.ShowMessage(R.string.error_no_network))
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            delay(700)
            _isRefreshing.value = false
        }
    }

}

data class HomeBikeScreenState(
    val uiState: HomeBikeUiState = HomeBikeUiState.Loading,
    val isRefreshing: Boolean = false,
    val selection: SelectionState = SelectionState(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)