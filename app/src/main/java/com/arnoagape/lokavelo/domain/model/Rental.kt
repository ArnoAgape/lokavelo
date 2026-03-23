package com.arnoagape.lokavelo.domain.model

import java.time.Instant

data class Rental(
    val id: String = "",
    val bikeId: String = "",
    val ownerId: String = "",
    val renterId: String = "",
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val priceTotalInCents: Long = 0,
    val basePriceInCents: Long = 0,
    val serviceFeeInCents: Long = 0,
    val depositInCents: Long? = null,
    val status: RentalStatus = RentalStatus.PENDING,
    val createdAt: Instant = Instant.now()
)

data class BikeWithRentals(
    val bike: Bike,
    val rentals: List<Rental>
)

data class RentalWithBike(
    val bike: Bike,
    val rental: Rental
)