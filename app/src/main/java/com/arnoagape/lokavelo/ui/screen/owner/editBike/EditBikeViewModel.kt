package com.arnoagape.lokavelo.ui.screen.owner.editBike

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for editing a new bike.
 *
 * Manages form state, validation, and user interactions
 * related to bike edition.
 */
@HiltViewModel
class EditBikeViewModel @Inject constructor(
    private val bikeRepository: BikeOwnerRepository
) : ViewModel() {

    private val bikeId = MutableStateFlow<String?>(null)

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())
    private val _remotePhotoUrls = MutableStateFlow<List<String>>(emptyList())
    private val _isSubmitting = MutableStateFlow(false)

    private val _originalBike = MutableStateFlow<Bike?>(null)

    private val _formState = MutableStateFlow(
        EditBikeFormState(
            title = "",
            description = "",
            location = BikeLocation(),
            priceText = "",
            depositText = ""
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val bikeFlow: StateFlow<EditBikeUiState> =
        bikeId
            .flatMapLatest { id ->
                if (id == null) {
                    flowOf(EditBikeUiState.Idle)
                } else {
                    bikeRepository.observeBike(id)
                        .map { bike ->
                            bike?.let {
                                EditBikeUiState.Loaded(it)
                            } ?: EditBikeUiState.Error.NotFound
                        }
                        .onStart { emit(EditBikeUiState.Loading) }
                        .catch { emit(EditBikeUiState.Error.Generic()) }
                }
            }
            .onEach { state ->
                if (state is EditBikeUiState.Loaded) {
                    val bike = state.bike
                    _originalBike.value = bike
                    _remotePhotoUrls.value = bike.photoUrls
                    _formState.value = EditBikeFormState(
                        title = bike.title,
                        description = bike.description,
                        location = bike.location,
                        priceText = (bike.priceInCents / 100).toString(),
                        depositText = bike.depositInCents?.div(100)?.toString() ?: "",
                        isElectric = bike.isElectric,
                        category = bike.category,
                        brand = bike.brand,
                        condition = bike.condition,
                        accessories = bike.accessories
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                EditBikeUiState.Idle
            )

    fun setBikeId(id: String) {
        bikeId.value = id
    }

    fun resetSubmitting() {
        _isSubmitting.value = false
    }

    fun removeRemotePhoto(url: String) {
        _remotePhotoUrls.update { it - url }
    }

    val state: StateFlow<EditBikeScreenState> =
        combine(
            bikeFlow,
            _formState,
            _localUris,
            _remotePhotoUrls,
            _isSubmitting
        ) { ui, form, uris, remote, submitting ->

            val totalPhotos = remote.size + uris.size

            EditBikeScreenState(
                uiState = if (submitting) EditBikeUiState.Submitting else ui,
                form = form,
                isValid = form.title.isNotBlank() &&
                        form.priceText.isNotBlank() &&
                        form.location.street.isNotBlank() &&
                        form.location.city.isNotBlank() &&
                        form.location.postalCode.isNotBlank() &&
                        totalPhotos > 0,
                localUris = uris,
                remotePhotoUrls = remote
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditBikeScreenState()
        )

    /**
     * Handles user actions modifying the bike or selected URIs.
     */
    fun onAction(event: EditBikeEvent) {
        when (event) {

            is EditBikeEvent.TitleChanged ->
                _formState.update { it.copy(title = event.title) }

            is EditBikeEvent.DescriptionChanged ->
                _formState.update { it.copy(description = event.description) }

            is EditBikeEvent.PriceChanged ->
                _formState.update { it.copy(priceText = event.priceText) }

            is EditBikeEvent.DepositChanged ->
                _formState.update { it.copy(depositText = event.depositText) }

            is EditBikeEvent.AddressChanged ->
                updateLocation { copy(street = event.address) }

            is EditBikeEvent.Address2Changed ->
                updateLocation { copy(addressLine2 = event.address2) }

            is EditBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is EditBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is EditBikeEvent.ElectricChanged ->
                _formState.update { it.copy(isElectric = event.isElectric) }

            is EditBikeEvent.CategoryChanged ->
                _formState.update { it.copy(category = event.category) }

            is EditBikeEvent.BrandChanged ->
                _formState.update { it.copy(brand = event.brand) }

            is EditBikeEvent.StateChanged ->
                _formState.update { it.copy(condition = event.state) }

            is EditBikeEvent.AccessoriesChanged ->
                _formState.update { it.copy(accessories = event.accessories) }

            is EditBikeEvent.AddPhoto ->
                _localUris.update { (it + event.uri).take(3) }

            is EditBikeEvent.RemovePhoto ->
                _localUris.update { it - event.uri }

            EditBikeEvent.Submit ->
                editBike()
        }
    }

    fun editBike() {
        viewModelScope.launch {

            val form = _formState.value
            val uris = _localUris.value
            val original = _originalBike.value ?: return@launch

            val totalPhotos = _remotePhotoUrls.value.size + uris.size

            if (!form.isValid(totalPhotos)) {
                _events.trySend(Event.ShowMessage(R.string.error_invalid_form))
                return@launch
            }

            _isSubmitting.value = true

            val updatedBike = form.toUpdatedBikeOrNull(original)
                ?: run {
                    _events.trySend(Event.ShowMessage(R.string.error_invalid_form))
                    return@launch
                }

            val finalBike = updatedBike.copy(
                photoUrls = _remotePhotoUrls.value
            )

            runCatching {
                bikeRepository.editBike(localUris = uris, bike = finalBike)
            }.onSuccess {

                _isSubmitting.value = true
                _events.trySend(Event.ShowSuccessMessage(R.string.success_bike_edited))

            }.onFailure { throwable ->
                Log.e("EditBike", "Error while editing bike", throwable)

                _isSubmitting.value = false
                _events.trySend(Event.ShowMessage(R.string.error_generic))
            }
        }
    }

    fun updateLocation(update: BikeLocation.() -> BikeLocation) {
        _formState.update {
            it.copy(location = it.location.update())
        }
    }
}

data class EditBikeScreenState(
    val uiState: EditBikeUiState = EditBikeUiState.Idle,
    val form: EditBikeFormState = EditBikeFormState(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList(),
    val remotePhotoUrls: List<String> = emptyList()
)