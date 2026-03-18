package com.arnoagape.lokavelo.ui.screen.owner.addBike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.GeocodingRepository
import com.arnoagape.lokavelo.domain.model.AddressSuggestion
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.map

/**
 * ViewModel responsible for adding a new bike.
 *
 * Manages form state, validation, and user interactions
 * related to bike edition.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddBikeViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val geocodingRepository: GeocodingRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

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

    private val _state = MutableStateFlow(AddBikeScreenState())
    val state: StateFlow<AddBikeScreenState> =
        combine(_state, suggestions) { state, suggestions ->
            state.copy(suggestions = suggestions)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AddBikeScreenState()
        )

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

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {

            if (!networkUtils.isNetworkAvailable()) {
                _state.update {
                    it.copy(
                        uiState = AddBikeUiState.Error.Initial(
                            isNetworkError = true
                        )
                    )
                }
            }
        }
    }

    fun retryInitialCheck() {
        _state.update { it.copy(uiState = AddBikeUiState.Idle) }
        checkInitialState()
    }

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

            is AddBikeEvent.PriceChanged -> {

                val cents = event.value.toCentsOrNull()

                _state.update { current ->

                    if (cents != null) {

                        val (twoDays, week, month) = calculateDerivedPrices(cents)

                        current.copy(
                            form = current.form.copy(
                                priceText = event.value,

                                isTwoDaysManuallyEdited = false,
                                isWeekManuallyEdited = false,
                                isMonthManuallyEdited = false,

                                twoDaysPriceText = twoDays.toPriceText(),
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

            is AddBikeEvent.TwoDaysPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            twoDaysPriceText = event.value,
                            isTwoDaysManuallyEdited = true
                        )
                    )
                }

            is AddBikeEvent.WeekPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            weekPriceText = event.value,
                            isWeekManuallyEdited = true
                        )
                    )
                }

            is AddBikeEvent.MonthPriceChanged ->
                _state.update {
                    it.copy(
                        form = it.form.copy(
                            monthPriceText = event.value,
                            isMonthManuallyEdited = true
                        )
                    )
                }

            is AddBikeEvent.DepositChanged ->
                _state.update {
                    it.copy(form = it.form.copy(depositText = event.depositText))
                }

            is AddBikeEvent.AddressChanged -> {
                addressQuery.value = event.address
                updateLocation {
                    copy(
                        street = event.address,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                }
            }

            is AddBikeEvent.ZipChanged ->
                updateLocation { copy(postalCode = event.zipCode) }

            is AddBikeEvent.CityChanged ->
                updateLocation { copy(city = event.city) }

            is AddBikeEvent.ElectricChanged ->
                _state.update {
                    it.copy(form = it.form.copy(electric = event.electric))
                }

            is AddBikeEvent.SizeChanged ->
                _state.update {
                    it.copy(form = it.form.copy(size = event.size))
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

            is AddBikeEvent.AvailableChanged ->
                _state.update {
                    it.copy(form = it.form.copy(available = event.available))
                }

            is AddBikeEvent.MinDaysRentalChanged ->
                _state.update {
                    it.copy(form = it.form.copy(minDaysRentalText = event.minDaysRentalText))
                }

            AddBikeEvent.Submit ->
                onPublishClicked()
        }
    }

    fun validateForm(): Boolean {

        val current = state.value.form
        val totalPhotos = state.value.photos.size

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

        val photosError = totalPhotos == 0

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
                _events.send(Event.ShowMessage(R.string.error_invalid_form))
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

            val bike = current.form.toBikeOrNull()
                ?: return@launch

            val localUris = current.photos
                .filterIsInstance<PhotoItem.Local>()
                .map { it.uri }

            _state.update {
                it.copy(isSaving = true)
            }

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
                    it.copy(
                        uiState = AddBikeUiState.Error.Generic(),
                        isSaving = false
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

data class AddBikeScreenState(
    val uiState: AddBikeUiState = AddBikeUiState.Idle,
    val form: AddBikeFormState = AddBikeFormState(),
    val isValid: Boolean = false,
    val photos: List<PhotoItem> = emptyList(),
    val isSaving: Boolean = false,
    val suggestions: List<AddressSuggestion> = emptyList()
)