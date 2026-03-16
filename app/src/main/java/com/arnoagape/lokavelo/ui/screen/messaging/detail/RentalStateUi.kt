package com.arnoagape.lokavelo.ui.screen.messaging.detail

import com.arnoagape.lokavelo.domain.model.Rental

sealed interface RentalStateUi {

    data class OwnerRequest(val rental: Rental) : RentalStateUi

    data class RenterWaiting(val rental: Rental) : RentalStateUi

    data class RenterCounterOffer(val rental: Rental) : RentalStateUi

    object None : RentalStateUi
}