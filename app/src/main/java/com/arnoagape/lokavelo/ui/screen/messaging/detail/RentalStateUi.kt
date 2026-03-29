package com.arnoagape.lokavelo.ui.screen.messaging.detail

import com.arnoagape.lokavelo.domain.model.Rental

sealed interface RentalStateUi {

    // === OWNER ===
    data class OwnerRequest(val rental: Rental) : RentalStateUi
    data class OwnerAccepted(val rental: Rental) : RentalStateUi
    data class OwnerDeclined(val rental: Rental) : RentalStateUi
    data class OwnerOfferSent(val rental: Rental) : RentalStateUi

    // === RENTER ===
    data class RenterWaiting(val rental: Rental) : RentalStateUi
    data class RenterOfferPending(val rental: Rental) : RentalStateUi
    data class RenterAccepted(val rental: Rental) : RentalStateUi
    data class RenterDeclined(val rental: Rental) : RentalStateUi

    object None : RentalStateUi
}