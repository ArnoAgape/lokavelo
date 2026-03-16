package com.arnoagape.lokavelo.ui.screen.main.map

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike

sealed class MapUiState {

    object Loading : MapUiState()
    data class Success(val bikes: List<Bike>) : MapUiState()

    sealed class Error : MapUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
        data class Network(@param:StringRes val messageRes: Int = R.string.error_no_network) : Error()
    }
}