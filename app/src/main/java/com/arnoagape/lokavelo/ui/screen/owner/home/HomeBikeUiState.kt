package com.arnoagape.lokavelo.ui.screen.owner.home

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike

sealed class HomeBikeUiState {
    object Loading : HomeBikeUiState()
    data class Success(val bikes: List<Bike>) : HomeBikeUiState()
    data class Empty(
        @param:StringRes val messageRes: Int = R.string.no_bike
    ) : HomeBikeUiState()

    sealed class Error : HomeBikeUiState() {
        data class Generic(@param:StringRes val messageRes: Int = R.string.error_generic) : Error()
        data class NotFound(
            @param:StringRes val messageRes: Int = R.string.error_no_bike_found
        ) : HomeBikeUiState()
    }
}