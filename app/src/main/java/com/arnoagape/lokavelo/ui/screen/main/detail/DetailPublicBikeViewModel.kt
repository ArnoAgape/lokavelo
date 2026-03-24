package com.arnoagape.lokavelo.ui.screen.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.User
import com.arnoagape.lokavelo.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailPublicBikeViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()
    private val _bikeId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(DetailPublicBikeState())

    val state: StateFlow<DetailPublicBikeState> =
        combine(
            _bikeId
                .filterNotNull()
                .flatMapLatest { id ->
                    bikeRepository.observeBike(id)
                        .filterNotNull()
                        .map { bike ->
                            val owner = userRepository.getUser(bike.ownerId)
                            Pair(bike, owner)
                        }
                },
            _uiState
        ) { (bike, owner), uiState ->
            uiState.copy(
                bike = bike,
                owner = owner,
                isLoading = false
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DetailPublicBikeState()
            )

    fun onContactClicked(): Boolean {
        val bike = state.value.bike ?: return false
        val currentUserId = userRepository.getCurrentUserId()

        return if (bike.ownerId == currentUserId) {
            _events.trySend(Event.ShowMessage(R.string.error_own_bike))
            false
        } else {
            true
        }
    }

    fun setBikeId(id: String) {
        _bikeId.value = id
    }

    fun setInitialDates(start: LocalDate?, end: LocalDate?) {
        _uiState.update {
            it.copy(
                startDate = start ?: it.startDate,
                endDate = end ?: it.endDate
            )
        }
    }

    fun updateDates(
        start: LocalDate,
        end: LocalDate
    ) {
        _uiState.update {
            it.copy(
                startDate = start,
                endDate = end
            )
        }
    }
}

data class DetailPublicBikeState(
    val bike: Bike? = null,
    val owner: User? = null,
    val isLoading: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(1),
    val rentals: List<Rental> = emptyList(),
    val error: Throwable? = null
)