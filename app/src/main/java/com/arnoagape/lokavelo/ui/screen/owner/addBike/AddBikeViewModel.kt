package com.arnoagape.lokavelo.ui.screen.owner.addBike

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBikeViewModel @Inject constructor(
    private val bikeRepository: BikeOwnerRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddBikeUiState>(AddBikeUiState.Idle)
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())

    private val isSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

    private val _formState = MutableStateFlow(
        AddFormState(
            title = "",
            description = "",
            location = BikeLocation(),
            priceText = "",
            depositText = ""
        )
    )

    val state: StateFlow<AddBikeScreenState> =
        combine(
            _uiState,
            _formState,
            _localUris,
            isSignedIn
        ) { ui, form, uris, signedIn ->
            AddBikeScreenState(
                uiState = ui,
                form = form,
                localUris = uris,
                isValid = form.title.isNotBlank() &&
                        form.priceText.isNotBlank() &&
                        form.location.street.isNotBlank() &&
                        form.location.city.isNotBlank() &&
                        form.location.postalCode.isNotBlank() &&
                        uris.isNotEmpty(),
                isSignedIn = signedIn
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AddBikeScreenState()
        )

    /**
     * Handles user actions modifying the file or selected URIs.
     */
    fun onAction(event: AddBikeEvent) {
        when (event) {

            is AddBikeEvent.TitleChanged ->
                _formState.update { it.copy(title = event.title) }

            is AddBikeEvent.DescriptionChanged ->
                _formState.update { it.copy(description = event.description) }

            is AddBikeEvent.PriceChanged ->
                _formState.update { it.copy(priceText = event.priceText) }

            is AddBikeEvent.DepositChanged ->
                _formState.update { it.copy(depositText = event.depositText) }

            is AddBikeEvent.AddressChanged ->
                updateLocation { copy(street = event.address) }

            is AddBikeEvent.Address2Changed ->
                updateLocation { copy(addressLine2 = event.address2) }

            is AddBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is AddBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is AddBikeEvent.ElectricChanged ->
                _formState.update { it.copy(isElectric = event.isElectric) }

            is AddBikeEvent.CategoryChanged ->
                _formState.update { it.copy(category = event.category) }

            is AddBikeEvent.BrandChanged ->
                _formState.update { it.copy(brand = event.brand) }

            is AddBikeEvent.StateChanged ->
                _formState.update { it.copy(condition = event.state) }

            is AddBikeEvent.AccessoriesChanged ->
                _formState.update { it.copy(accessories = event.accessories) }

            is AddBikeEvent.AddPhoto ->
                _localUris.update { (it + event.uri).take(3) }

            is AddBikeEvent.RemovePhoto ->
                _localUris.update { it - event.uri }

            AddBikeEvent.Submit ->
                addBike()
        }
    }

    /**
     * Uploads the selected files to Firebase Storage and Firestore.
     * Performs network and authentication checks before uploading.
     */
    fun addBike() {
        viewModelScope.launch {

            val form = _formState.value
            val uris = _localUris.value

            if (!form.isValid(uris)) {
                _events.trySend(Event.ShowMessage(R.string.error_invalid_form))
                return@launch
            }

            _uiState.value = AddBikeUiState.Loading

            val bike = form.toBikeOrNull()

            if (bike == null) {
                _events.trySend(Event.ShowMessage(R.string.error_invalid_form))
                return@launch
            }

            runCatching {
                bikeRepository.addBike(
                    localUris = uris,
                    bike = bike
                )
            }.onSuccess {
                _uiState.value = AddBikeUiState.Success
                _localUris.value = emptyList()
                _formState.value = AddFormState()
                _events.trySend(Event.ShowSuccessMessage(R.string.success_bike_added))
            }.onFailure { throwable ->
                Log.e("AddBike", "Error while adding bike", throwable)

                _uiState.value = AddBikeUiState.Error.Generic()
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

data class AddBikeScreenState(
    val uiState: AddBikeUiState = AddBikeUiState.Idle,
    val form: AddFormState = AddFormState(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList(),
    val isSignedIn: Boolean = false
)