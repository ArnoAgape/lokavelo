package com.arnoagape.lokavelo.ui.screen.owner.addBike

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBikeViewModel @Inject constructor(
    private val bikeRepository: BikeOwnerRepository
) : ViewModel() {

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _state = MutableStateFlow(AddBikeScreenState())
    val state: StateFlow<AddBikeScreenState> = _state

    val hasUnsavedChanges: StateFlow<Boolean> =
        state.map { current ->

            val initialForm = AddBikeFormState()

            val formChanged = current.form != initialForm
            val photosChanged = current.localUris.isNotEmpty()

            formChanged || photosChanged

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    /**
     * Handles user actions modifying the bike or selected URIs.
     */
    fun onAction(event: AddBikeEvent) {
        when (event) {

            is AddBikeEvent.TitleChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(title = event.title))
                }

            is AddBikeEvent.DescriptionChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(description = event.description))
                }

            is AddBikeEvent.PriceChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(priceText = event.priceText))
                }

            is AddBikeEvent.DepositChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(depositText = event.depositText))
                }

            is AddBikeEvent.AddressChanged ->
                updateLocation { copy(street = event.address) }

            is AddBikeEvent.Address2Changed ->
                updateLocation { copy(addressLine2 = event.address2) }

            is AddBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is AddBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is AddBikeEvent.ElectricChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(isElectric = event.isElectric))
                }

            is AddBikeEvent.CategoryChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(category = event.category))
                }

            is AddBikeEvent.BrandChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(brand = event.brand))
                }

            is AddBikeEvent.StateChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(condition = event.state))
                }

            is AddBikeEvent.AccessoriesChanged ->
                _state.updateState {
                    it.copy(form = it.form.copy(accessories = event.accessories))
                }

            is AddBikeEvent.AddPhoto ->
                _state.updateState { current ->

                    if (current.localUris.size >= 3) current
                    else current.copy(
                        localUris = current.localUris + event.uri
                    )
                }

            is AddBikeEvent.RemovePhoto ->
                _state.updateState {
                    it.copy(localUris = it.localUris - event.uri)
                }

            is AddBikeEvent.ReplacePhoto ->
                _state.updateState { current ->

                    val index = current.localUris.indexOfFirst {
                        it.toString() == event.oldUri.toString()
                    }

                    if (index == -1) current
                    else {
                        val updated = current.localUris.toMutableList()
                        updated[index] = event.newUri
                        current.copy(localUris = updated)
                    }
                }

            AddBikeEvent.Submit ->
                addBike()
        }
    }

    /**
     * Uploads the selected files to Firebase Storage and Firestore.
     * Performs network and authentication checks before uploading.
     */
    private fun addBike() {

        viewModelScope.launch {

            val current = _state.value
            val totalPhotos = current.localUris.size

            if (!current.form.isValid(totalPhotos)) {
                _events.trySend(Event.ShowMessage(R.string.error_invalid_form))
                return@launch
            }

            _state.update {
                it.copy(uiState = AddBikeUiState.Submitting)
            }

            val bike = current.form.toBikeOrNull()
                ?: return@launch

            runCatching {
                bikeRepository.addBike(
                    localUris = current.localUris,
                    bike = bike
                )
            }.onSuccess {

                _events.trySend(
                    Event.ShowSuccessMessage(R.string.success_bike_added)
                )

                _state.value = AddBikeScreenState()

            }.onFailure { throwable ->

                Log.e("AddBike", "Error while adding bike", throwable)

                _state.update {
                    it.copy(uiState = AddBikeUiState.Error.Generic())
                }

                _events.trySend(Event.ShowMessage(R.string.error_generic))
            }
        }
    }

    private fun updateLocation(update: BikeLocation.() -> BikeLocation) {
        _state.updateState {
            it.copy(
                form = it.form.copy(
                    location = it.form.location.update()
                )
            )
        }
    }

    private fun computeIsValid(state: AddBikeScreenState): Boolean {

        val totalPhotos = state.localUris.size

        return state.form.isValid(totalPhotos)
    }

    private fun MutableStateFlow<AddBikeScreenState>.updateState(
        reducer: (AddBikeScreenState) -> AddBikeScreenState
    ) {
        update { current ->
            val updated = reducer(current)
            updated.copy(isValid = computeIsValid(updated))
        }
    }

}

data class AddBikeScreenState(
    val uiState: AddBikeUiState = AddBikeUiState.Idle,
    val form: AddBikeFormState = AddBikeFormState(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList()
)