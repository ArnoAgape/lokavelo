package com.arnoagape.lokavelo.ui.screen.main.home

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike

sealed class HomeScreenUiState {

    object Loading : HomeScreenUiState()
    object Empty : HomeScreenUiState()
    data class Success(val bikes: List<Bike>) : HomeScreenUiState()

    sealed class Error : HomeScreenUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
        data class Network(@param:StringRes val messageRes: Int = R.string.error_no_network) : Error()
    }
}