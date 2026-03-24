package com.arnoagape.lokavelo.ui.screen.bikes

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Rental
import java.time.LocalDate

sealed interface BikeItemContext {
    // Garage du propriétaire
    data class OwnerGarage(val bike: Bike) : BikeItemContext

    // Location côté locataire : dates, badge statut, prix total
    data class RenterRental(
        val bike: Bike,
        val rental: Rental,
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : BikeItemContext

    // Location côté propriétaire : dates, badge statut, prix net proprio
    data class OwnerRental(
        val bike: Bike,
        val rental: Rental,
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : BikeItemContext
}