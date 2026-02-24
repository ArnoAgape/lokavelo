package com.arnoagape.lokavelo.ui.screen.owner.editBike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeOwnerRepository
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull
import com.arnoagape.lokavelo.ui.utils.toPriceString
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.toMutableList

/**
 * ViewModel responsible for editing a new bike.
 *
 * Manages form state, validation, and user interactions
 * related to bike edition.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditBikeViewModel @Inject constructor(
    private val bikeRepository: BikeOwnerRepository
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val bikeId = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow(EditBikeScreenState())
    val state: StateFlow<EditBikeScreenState> = _state

    fun setBikeId(id: String) {
        bikeId.value = id
        observeBike()
    }

    private fun observeBike() {
        viewModelScope.launch {
            bikeId
                .filterNotNull()
                .flatMapLatest { id ->
                    bikeRepository.observeBike(id)
                }
                .collect { bike ->

                    if (bike == null) {
                        _state.update {
                            it.copy(uiState = EditBikeUiState.Error.NotFound)
                        }
                        return@collect
                    }

                    _state.update {
                        it.copy(
                            uiState = EditBikeUiState.Loaded(bike),
                            form = EditBikeFormState.fromBike(bike),
                            photos = bike.photoUrls.map { url ->
                                PhotoItem.Remote(
                                    id = UUID.randomUUID().toString(),
                                    url = url
                                )
                            }
                        )
                    }
                }
        }
    }

    val hasUnsavedChanges: StateFlow<Boolean> =
        state.map { current ->

            val original =
                (current.uiState as? EditBikeUiState.Loaded)?.bike
                    ?: return@map false

            val formChanged =
                current.form != EditBikeFormState.fromBike(original)

            val currentRemoteUrls = current.photos
                .filterIsInstance<PhotoItem.Remote>()
                .map { it.url }

            val hasLocal = current.photos.any { it is PhotoItem.Local }

            val photosChanged =
                hasLocal || currentRemoteUrls != original.photoUrls

            formChanged || photosChanged

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    fun onAction(event: EditBikeEvent) {

        when (event) {

            is EditBikeEvent.TitleChanged ->
                _state.update {
                    it.copy(form = it.form.copy(title = event.title))
                }

            is EditBikeEvent.DescriptionChanged ->
                _state.update {
                    it.copy(form = it.form.copy(description = event.description))
                }

            is EditBikeEvent.PriceChanged -> {

                val cents = event.value.toCentsOrNull()

                _state.update { current ->

                    if (cents != null) {

                        val (half, week, month) = calculateDerivedPrices(cents)

                        current.copy(
                            form = current.form.copy(
                                priceText = event.value,

                                isHalfDayManuallyEdited = false,
                                isWeekManuallyEdited = false,
                                isMonthManuallyEdited = false,

                                halfDayPriceText = half.toPriceString(),
                                weekPriceText = week.toPriceString(),
                                monthPriceText = month.toPriceString()
                            )
                        )
                    } else {
                        current.copy(
                            form = current.form.copy(
                                priceText = event.value,
                                halfDayPriceText = "",
                                weekPriceText = "",
                                monthPriceText = ""
                            )
                        )
                    }
                }
            }

            is EditBikeEvent.HalfDayPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            halfDayPriceText = event.value,
                            isHalfDayManuallyEdited = true
                        )
                    )
                }

            is EditBikeEvent.WeekPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            weekPriceText = event.value,
                            isWeekManuallyEdited = true
                        )
                    )
                }

            is EditBikeEvent.MonthPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            monthPriceText = event.value,
                            isMonthManuallyEdited = true
                        )
                    )
                }

            is EditBikeEvent.DepositChanged ->
                _state.update {
                    it.copy(form = it.form.copy(depositText = event.depositText))
                }

            is EditBikeEvent.AddressChanged ->
                updateLocation { copy(street = event.address) }

            is EditBikeEvent.Address2Changed ->
                updateLocation { copy(addressLine2 = event.address2) }

            is EditBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is EditBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is EditBikeEvent.ElectricChanged ->
                _state.update {
                    it.copy(form = it.form.copy(electric = event.electric))
                }

            is EditBikeEvent.CategoryChanged ->
                _state.update {
                    it.copy(form = it.form.copy(category = event.category))
                }

            is EditBikeEvent.BrandChanged ->
                _state.update {
                    it.copy(form = it.form.copy(brand = event.brand))
                }

            is EditBikeEvent.StateChanged ->
                _state.update {
                    it.copy(form = it.form.copy(condition = event.state))
                }

            is EditBikeEvent.AccessoriesChanged ->
                _state.update {
                    it.copy(form = it.form.copy(accessories = event.accessories))
                }

            is EditBikeEvent.AddPhoto ->
                _state.update { current ->

                    if (current.photos.size >= 3) {
                        current
                    } else {
                        current.copy(
                            photos = current.photos + PhotoItem.Local(
                                id = UUID.randomUUID().toString(),
                                uri = event.uri
                            )
                        )
                    }
                }

            is EditBikeEvent.RemovePhoto ->
                _state.update {
                    it.copy(
                        photos = it.photos.filterNot { photo -> photo.id == event.id }
                    )
                }

            is EditBikeEvent.ReplacePhoto ->
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

            EditBikeEvent.Submit ->
                onPublishClicked()
        }
    }

    fun validateForm(): Boolean {

        val current = state.value.form
        val totalPhotos = state.value.photos.size
        val photosError = totalPhotos == 0

        val titleError = current.title.isBlank()
        val categoryError = current.category == null
        val conditionError = current.condition == null

        val price = current.priceText.toCentsOrNull()
        val priceError = price == null || price <= 0

        val streetError = current.location.street.isBlank()
        val postalCodeError = current.location.postalCode.isBlank()
        val cityError = current.location.city.isBlank()

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

        editBike()
    }

    private fun editBike() {

        viewModelScope.launch {

            val current = _state.value
            val original =
                (current.uiState as? EditBikeUiState.Loaded)?.bike
                    ?: return@launch

            _state.update {
                it.copy(uiState = EditBikeUiState.Submitting)
            }

            val updatedBike =
                current.form.toUpdatedBikeOrNull(original)
                    ?: return@launch

            val remoteUrls = current.photos
                .filterIsInstance<PhotoItem.Remote>()
                .map { it.url }

            val localUris = current.photos
                .filterIsInstance<PhotoItem.Local>()
                .map { it.uri }

            val finalBike = updatedBike.copy(
                photoUrls = remoteUrls
            )

            runCatching {
                bikeRepository.editBike(
                    localUris = localUris,
                    bike = finalBike
                )
            }.onSuccess {

                _events.trySend(
                    Event.ShowSuccessMessage(R.string.success_bike_edited)
                )

            }.onFailure { throwable ->

                Log.e("EditBike", "Error while editing bike", throwable)

                _state.update {
                    it.copy(uiState = EditBikeUiState.Error.Generic())
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

    private fun calculateDerivedPrices(dayPriceCents: Long): Triple<Long, Long, Long> {

        val half = dayPriceCents / 2

        val week = (dayPriceCents * 7 * 70) / 100   // -30%
        val month = (dayPriceCents * 30 * 50) / 100 // -50%

        return Triple(half, week, month)
    }
}

data class EditBikeScreenState(
    val uiState: EditBikeUiState = EditBikeUiState.Idle,
    val form: EditBikeFormState = EditBikeFormState(),
    val isValid: Boolean = false,
    val photos: List<PhotoItem> = emptyList()
)