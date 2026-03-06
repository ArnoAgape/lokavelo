package com.arnoagape.lokavelo.ui.screen.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
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


    private val _bikeId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(DetailPublicBikeState())

    private val bikeFlow =
        _bikeId
            .filterNotNull()
            .flatMapLatest { bikeRepository.observeBike(it) }

    private val ownerFlow =
        bikeFlow
            .filterNotNull()
            .flatMapLatest { bike ->
                userRepository.observeUser(bike.ownerId)
            }

    val state: StateFlow<DetailPublicBikeState> =
        combine(_uiState, bikeFlow, ownerFlow) { ui, bike, owner ->

            ui.copy(
                bike = bike,
                owner = owner,
                isLoading = false
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DetailPublicBikeState()
        )

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