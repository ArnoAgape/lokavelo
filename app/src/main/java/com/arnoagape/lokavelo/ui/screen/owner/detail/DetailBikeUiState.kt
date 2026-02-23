package com.arnoagape.lokavelo.ui.screen.owner.detail

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike

/**
 * Represents the UI state for the detail screen.
 */
sealed class DetailBikeUiState {

    object Loading : DetailBikeUiState()
    data class Success(val bike: Bike) : DetailBikeUiState()
    object Deleting : DetailBikeUiState()
    sealed class Error : DetailBikeUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
    }
}