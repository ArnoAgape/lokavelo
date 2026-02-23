package com.arnoagape.lokavelo.ui.screen.owner.addBike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
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
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.map

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
            val photosChanged = current.photos.isNotEmpty()

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
                _state.update {
                    it.copy(form = it.form.copy(title = event.title))
                }

            is AddBikeEvent.DescriptionChanged ->
                _state.update {
                    it.copy(form = it.form.copy(description = event.description))
                }

            is AddBikeEvent.PriceChanged ->
                _state.update {
                    it.copy(form = it.form.copy(priceText = event.priceText))
                }

            is AddBikeEvent.DepositChanged ->
                _state.update {
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
                _state.update {
                    it.copy(form = it.form.copy(isElectric = event.isElectric))
                }

            is AddBikeEvent.CategoryChanged ->
                _state.update {
                    it.copy(form = it.form.copy(category = event.category))
                }

            is AddBikeEvent.BrandChanged ->
                _state.update {
                    it.copy(form = it.form.copy(brand = event.brand))
                }

            is AddBikeEvent.StateChanged ->
                _state.update {
                    it.copy(form = it.form.copy(condition = event.state))
                }

            is AddBikeEvent.AccessoriesChanged ->
                _state.update {
                    it.copy(form = it.form.copy(accessories = event.accessories))
                }

            is AddBikeEvent.AddPhoto ->
                _state.update { current ->

                    if (current.photos.size >= 3) current
                    else current.copy(
                        photos = current.photos + PhotoItem.Local(
                            id = UUID.randomUUID().toString(),
                            uri = event.uri
                        )
                    )
                }

            is AddBikeEvent.RemovePhoto ->
                _state.update {
                    it.copy(
                        photos = it.photos.filterNot { photo -> photo.id == event.id }
                    )
                }

            is AddBikeEvent.ReplacePhoto ->
                _state.update { current ->

                    val updated = current.photos.map { photo ->
                        if (photo.id == event.id) {
                            PhotoItem.Local(
                                id = photo.id,
                                uri = event.newUri
                            )
                        } else photo
                    }

                    current.copy(photos = updated)
                }

            AddBikeEvent.Submit ->
                onPublishClicked()
        }
    }

    fun validateForm(): Boolean {

        val current = state.value.form
        val totalPhotos = state.value.photos.size

        val titleError = current.title.isBlank()
        val categoryError = current.category == null
        val conditionError = current.condition == null

        val price = current.priceText
            .replace(",", ".")
            .toDoubleOrNull()

        val priceError = price == null || price <= 0

        val streetError = current.location.street.isBlank()
        val postalCodeError = current.location.postalCode.isBlank()
        val cityError = current.location.city.isBlank()

        val photosError = totalPhotos == 0

        _state.update {
            it.copy(
                form = current.copy(
                    titleError = titleError,
                    categoryError = categoryError,
                    conditionError = conditionError,
                    priceError = priceError,
                    streetError = streetError,
                    postalCodeError = postalCodeError,
                    cityError = cityError,
                    photosError = photosError
                )
            )
        }

        return !(titleError ||
                categoryError ||
                conditionError ||
                priceError ||
                streetError ||
                postalCodeError ||
                cityError ||
                photosError)
    }

    private fun onPublishClicked() {
        if (!validateForm()) {
            viewModelScope.launch {
                _events.send(Event.ShowMessage(R.string.error_invalid_form))
            }
            return
        }

        addBike()
    }

    /**
     * Uploads the selected files to Firebase Storage and Firestore.
     * Performs network and authentication checks before uploading.
     */
    private fun addBike() {

        viewModelScope.launch {

            val current = _state.value

            _state.update {
                it.copy(uiState = AddBikeUiState.Submitting)
            }

            val bike = current.form.toBikeOrNull()
                ?: return@launch

            val localUris = current.photos
                .filterIsInstance<PhotoItem.Local>()
                .map { it.uri }

            runCatching {
                bikeRepository.addBike(
                    localUris = localUris,
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
        _state.update {
            it.copy(
                form = it.form.copy(
                    location = it.form.location.update()
                )
            )
        }
    }

    fun movePhoto(from: Int, to: Int) {
        _state.update { current ->
            val mutable = current.photos.toMutableList()
            val item = mutable.removeAt(from)
            mutable.add(to, item)

            current.copy(photos = mutable)
        }
    }

}

data class AddBikeScreenState(
    val uiState: AddBikeUiState = AddBikeUiState.Idle,
    val form: AddBikeFormState = AddBikeFormState(),
    val isValid: Boolean = false,
    val photos: List<PhotoItem> = emptyList()
)