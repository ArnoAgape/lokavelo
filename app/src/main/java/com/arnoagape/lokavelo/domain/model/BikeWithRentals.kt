package com.arnoagape.lokavelo.domain.model

data class BikeWithRentals(
    val bike: Bike,
    val rentals: List<Rental>
)