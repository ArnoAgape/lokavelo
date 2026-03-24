package com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike

import com.arnoagape.lokavelo.domain.model.Bike

/**
 * Represents the UI state for the detail screen.
 */
sealed class DetailBikeUiState {

    object Idle : DetailBikeUiState()
    object Loading : DetailBikeUiState()
    object Deleting : DetailBikeUiState()
    data class Success(val bike: Bike) : DetailBikeUiState()
    sealed class Error : DetailBikeUiState() {
        data class Generic(val message: String = "Unknown error") : Error()
        data class Network(val isNetworkError: Boolean) : Error()
    }
}