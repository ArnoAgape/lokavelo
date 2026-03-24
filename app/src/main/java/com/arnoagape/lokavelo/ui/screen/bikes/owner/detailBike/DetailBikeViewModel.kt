package com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for loading a single bike and exposing UI state
 * for the detail screen.
 *
 * It observes the bike in Firestore, handles loading/error states,
 * and emits one-time events such as network warnings.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailBikeViewModel @Inject constructor(
    private val bikeRepository: BikeRepository
) : ViewModel() {

    private val _events = Channel<DetailBikeEvent>()
    val eventsFlow = _events.receiveAsFlow()
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog
    private val _bikeId = MutableStateFlow<String?>(null)
    private val _isDeleting = MutableStateFlow(false)

    fun setBikeId(id: String) {
        _bikeId.value = id
    }

    fun requestDeleteConfirmation() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    private val bikeState: StateFlow<DetailBikeUiState> =
        _bikeId
            .filterNotNull()
            .flatMapLatest { bikeId ->
                bikeRepository.observeOwnerBike(bikeId)
            }
            .map { bike ->

                when {
                    _isDeleting.value ->
                        DetailBikeUiState.Deleting

                    bike == null ->
                        DetailBikeUiState.Idle

                    else ->
                        DetailBikeUiState.Success(bike)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DetailBikeUiState.Idle
            )

    val state: StateFlow<DetailScreenState> =
        bikeState
            .map { ui ->
                DetailScreenState(
                    bikeState = ui
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DetailScreenState()
            )

    fun deleteBike() = viewModelScope.launch {

        val bike = (bikeState.value as? DetailBikeUiState.Success)?.bike

        if (bike == null) {
            _events.trySend(DetailBikeEvent.ShowMessage(R.string.error_editing_bike))
            return@launch
        }

        _isDeleting.value = true

        val result = bikeRepository.deleteBikes(setOf(bike.id))

        if (result.isSuccess) {
            _events.trySend(DetailBikeEvent.ShowSuccessMessage(R.string.success_bike_deleted))
        } else {
            _isDeleting.value = false
            _events.trySend(DetailBikeEvent.ShowMessage(R.string.error_delete_bike))
        }
    }

    fun onEditClicked(bikeId: String) {
        viewModelScope.launch {
            _events.send(DetailBikeEvent.NavigateToEdit(bikeId))
        }
    }
}

data class DetailScreenState(
    val bikeState: DetailBikeUiState = DetailBikeUiState.Idle,
    val isSignedIn: Boolean = false
)