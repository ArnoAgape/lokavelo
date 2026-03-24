package com.arnoagape.lokavelo.ui.screen.bikes.owner.editBike

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike

sealed class EditBikeUiState {

    object Idle : EditBikeUiState()
    object Loading : EditBikeUiState()
    data class Loaded(val bike: Bike) : EditBikeUiState()
    sealed class Error : EditBikeUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
        data class Network(val isNetworkError: Boolean) : Error()
        data class Initial(val isNetworkError: Boolean) : Error()
    }
}