package com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike

/**
 * Represents one-time UI events sent from ViewModels to the UI layer.
 *
 * These events are transient actions such as showing messages or
 * triggering navigation, and are not part of the persistent UI state.
 */
sealed interface DetailBikeEvent {

    /**
     * Shows a message to the user using a string resource.
     */
    data class ShowMessage(val message: Int) : DetailBikeEvent

    /**
     * Shows a success message to the user using a string resource.
     */
    data class ShowSuccessMessage(val message: Int) : DetailBikeEvent

    data class NavigateToEdit(val bikeId: String) : DetailBikeEvent
}
