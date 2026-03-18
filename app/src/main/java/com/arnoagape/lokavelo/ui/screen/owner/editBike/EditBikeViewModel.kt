package com.arnoagape.lokavelo.ui.screen.owner.editBike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.GeocodingRepository
import com.arnoagape.lokavelo.domain.model.AddressSuggestion
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull
import com.arnoagape.lokavelo.ui.utils.toPriceText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.toMutableList

/**
 * ViewModel responsible for editing a bike.
 *
 * Manages form state, validation, and user interactions
 * related to bike edition.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class EditBikeViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val geocodingRepository: GeocodingRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val bikeId = MutableStateFlow<String?>(null)

    private val addressQuery = MutableStateFlow("")

    val suggestions: StateFlow<List<AddressSuggestion>> =
        addressQuery
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

    private val bikeFlow: Flow<Bike> =
        bikeId
            .filterNotNull()
            .flatMapLatest { id ->
                bikeRepository.observeOwnerBike(id)
            }
            .filterNotNull()

    private val _state = MutableStateFlow(EditBikeScreenState())

    val state: StateFlow<EditBikeScreenState> =
        combine(_state, suggestions) { state, suggestions ->
            state.copy(suggestions = suggestions)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            EditBikeScreenState()
        )

    private var hasLoaded = false

    fun setBikeId(id: String) {
        if (hasLoaded) return

        hasLoaded = true
        bikeId.value = id
        observeBike()
    }

    private fun observeBike() {
        viewModelScope.launch {

            _state.update {
                it.copy(uiState = EditBikeUiState.Loading)
            }

            runCatching {
                bikeFlow.firstOrNull()
            }.onSuccess { bike ->

                if (bike == null) {

                    _state.update {
                        it.copy(
                            uiState = EditBikeUiState.Error.Initial(
                                isNetworkError = !networkUtils.isNetworkAvailable()
                            )
                        )
                    }

                    return@onSuccess
                }

                _state.update {
                    it.copy(
                        uiState = EditBikeUiState.Loaded(bike),
                        form = EditBikeFormState.fromBike(bike),
                        photos = bike.photoUrls.map { url ->
                            PhotoItem.Remote(id = url, url = url)
                        },
                        isFormInitialized = true
                    )
                }

            }.onFailure {

                _state.update {
                    it.copy(
                        uiState = EditBikeUiState.Error.Initial(
                            isNetworkError = !networkUtils.isNetworkAvailable()
                        )
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

                                isTwoDaysManuallyEdited = false,
                                isWeekManuallyEdited = false,
                                isMonthManuallyEdited = false,

                                twoDaysPriceText = half.toPriceText(),
                                weekPriceText = week.toPriceText(),
                                monthPriceText = month.toPriceText()
                            )
                        )
                    } else {
                        current.copy(
                            form = current.form.copy(
                                priceText = event.value,
                                twoDaysPriceText = "",
                                weekPriceText = "",
                                monthPriceText = ""
                            )
                        )
                    }
                }
            }

            is EditBikeEvent.TwoDaysPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            twoDaysPriceText = event.value,
                            isTwoDaysManuallyEdited = true
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

            is EditBikeEvent.AddressChanged -> {
                addressQuery.value = event.address
                updateLocation {
                    copy(
                        street = event.address,
                        longitude = 0.0,
                        latitude = 0.0
                    )
                }
            }

            is EditBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is EditBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is EditBikeEvent.ElectricChanged ->
                _state.update {
                    it.copy(form = it.form.copy(electric = event.electric))
                }

            is EditBikeEvent.SizeChanged ->
                _state.update {
                    it.copy(form = it.form.copy(size = event.size))
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

            is EditBikeEvent.AvailableChanged ->
                _state.update {
                    it.copy(form = it.form.copy(available = event.available))
                }

            is EditBikeEvent.MinDaysRentalChanged ->
                _state.update {
                    it.copy(form = it.form.copy(minDaysRentalText = event.minDaysRental))
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
        val descriptionError = current.description.isBlank()
        val categoryError = current.category == null
        val conditionError = current.condition == null
        val minDaysRentalError = current.minDaysRentalText.toIntOrNull()?.let { it < 1 } ?: true

        val price = current.priceText.toCentsOrNull()
        val priceError = price == null || price <= 0

        val streetError = current.location.street.isBlank()
        val postalCodeError = current.location.postalCode.isBlank()
        val cityError = current.location.city.isBlank()

        _state.update {
            it.copy(
                form = current.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    categoryError = categoryError,
                    conditionError = conditionError,
                    priceError = priceError,
                    streetError = streetError,
                    postalCodeError = postalCodeError,
                    cityError = cityError,
                    photosError = photosError,
                    minDaysRentalError = minDaysRentalError
                )
            )
        }

        return !(titleError ||
                descriptionError ||
                categoryError ||
                conditionError ||
                priceError ||
                streetError ||
                postalCodeError ||
                cityError ||
                photosError ||
                minDaysRentalError)
    }

    private fun onPublishClicked() {

        if (!validateForm()) {
            viewModelScope.launch {
                _events.send(
                    Event.ShowMessage(R.string.error_invalid_form)
                )
            }
            return
        }

        viewModelScope.launch {

            if (!networkUtils.isNetworkAvailable()) {
                _events.send(
                    Event.ShowMessage(R.string.error_no_network)
                )
                return@launch
            }

            editBike()
        }
    }

    private fun editBike() {

        viewModelScope.launch {

            val current = _state.value
            val original =
                (current.uiState as? EditBikeUiState.Loaded)?.bike
                    ?: return@launch

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

            _state.update {
                it.copy(isSaving = true)
            }

            runCatching {
                bikeRepository.editBike(
                    localUris = localUris,
                    bike = finalBike
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                _events.trySend(
                    Event.ShowSuccessMessage(R.string.success_bike_edited)
                )

            }.onFailure { throwable ->
                Log.e("EditBike", "Error while editing bike", throwable)
                _state.update {
                    it.copy(
                        isSaving = false,
                        uiState = EditBikeUiState.Error.Generic()
                    )
                }

                _events.trySend(Event.ShowMessage(R.string.error_generic))
            }
        }
    }

    fun onSuggestionSelected(suggestion: AddressSuggestion) {
        _state.update {
            it.copy(
                form = it.form.copy(
                    location = BikeLocation(
                        street = suggestion.street ?: "",
                        postalCode = suggestion.postalCode ?: "",
                        city = suggestion.city ?: "",
                        country = suggestion.country ?: "",
                        latitude = suggestion.lat,
                        longitude = suggestion.lon
                    )
                ),
                suggestions = emptyList()
            )
        }

        addressQuery.value = ""
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

        val twoDays = (dayPriceCents * 2 * 90) / 100   // -10%
        val week = (dayPriceCents * 7 * 70) / 100      // -30%
        val month = (dayPriceCents * 30 * 50) / 100    // -50%

        return Triple(twoDays, week, month)
    }

}

data class EditBikeScreenState(
    val uiState: EditBikeUiState = EditBikeUiState.Idle,
    val form: EditBikeFormState = EditBikeFormState(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false,
    val isFormInitialized: Boolean = false,
    val photos: List<PhotoItem> = emptyList(),
    val suggestions: List<AddressSuggestion> = emptyList()
)