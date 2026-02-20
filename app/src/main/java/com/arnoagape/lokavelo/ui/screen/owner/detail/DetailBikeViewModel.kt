package com.arnoagape.lokavelo.ui.screen.owner.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private val bikeRepository: BikeOwnerRepository,
    userRepository: UserRepository,
) : ViewModel() {

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog
    private val _bikeId = MutableStateFlow<String?>(null)

    fun setBikeId(id: String) {
        _bikeId.value = id
    }

    fun requestDeleteConfirmation() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    private val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val bikeState: StateFlow<DetailBikeUiState> =
        _bikeId
            .filterNotNull()
            .flatMapLatest { bikeId ->
                bikeRepository.observeBike(bikeId)
            }
            .map { bike ->
                when (bike) {
                    null -> DetailBikeUiState.Error.Empty("Impossible to find the bike")
                    else -> DetailBikeUiState.Success(bike)
                }
            }
            .onStart { emit(DetailBikeUiState.Loading) }
            .catch { e ->
                Log.e("DETAIL_FLOW", "Flow error", e)
                emit(
                    DetailBikeUiState.Error.Generic(
                        e.message ?: "Unknown error"
                    )
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DetailBikeUiState.Loading
            )

    val state: StateFlow<DetailScreenState> =
        combine(
            bikeState,
            isUserSignedIn
        ) { ui, signedIn ->
            DetailScreenState(
                bikeState = ui,
                isSignedIn = signedIn == true
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailScreenState()
        )

    fun deleteBike() = viewModelScope.launch {
        val bike = (bikeState.value as? DetailBikeUiState.Success)?.bike

        if (bike == null) {
            _events.trySend(Event.ShowMessage(R.string.error_editing_bike))
            return@launch
        }

        val result = bikeRepository.deleteBikes(setOf(bike.id))

        if (result.isSuccess) {
            _events.trySend(Event.ShowSuccessMessage(R.string.success_bike_deleted))
        } else {
            Log.e("DELETE_DEBUG", "Delete failed", result.exceptionOrNull())
            _events.trySend(Event.ShowMessage(R.string.error_delete_bike))
        }
    }
}

data class DetailScreenState(
    val bikeState: DetailBikeUiState = DetailBikeUiState.Loading,
    val isSignedIn: Boolean = false
)