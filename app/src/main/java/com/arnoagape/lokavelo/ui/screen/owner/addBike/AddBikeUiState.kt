package com.arnoagape.lokavelo.ui.screen.owner.addBike

sealed class AddBikeUiState {

    object Idle : AddBikeUiState()
    object Loading : AddBikeUiState()
    sealed class Error : AddBikeUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}