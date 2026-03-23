package com.arnoagape.lokavelo.ui.screen.owner.homeBike

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeWithRentals

sealed class HomeBikeUiState {
    object Loading : HomeBikeUiState()
    data class Success(val bikes: List<BikeWithRentals>) : HomeBikeUiState()
    object Empty : HomeBikeUiState()
    object SearchEmpty : HomeBikeUiState()

    sealed class Error : HomeBikeUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
    }
}