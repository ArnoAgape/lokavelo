package com.arnoagape.lokavelo.ui.screen.rental

import com.arnoagape.lokavelo.ui.screen.owner.homeBike.RentalWithBike

sealed interface HomeRentalUiState {
    data object Loading : HomeRentalUiState
    data class Success(
        val pending: List<RentalWithBike>,
        val active: List<RentalWithBike>,
        val history: List<RentalWithBike>
    ) : HomeRentalUiState
    data object Empty : HomeRentalUiState

    sealed interface Error : HomeRentalUiState {
        data object Generic : Error
    }
}