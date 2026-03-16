package com.arnoagape.lokavelo.data.service.rental

import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import kotlinx.coroutines.flow.Flow

interface RentalApi {

    suspend fun createRental(rental: Rental)

    fun observeOwnerRentals(): Flow<List<Rental>>

    fun observeRental(conversationId: String): Flow<Rental?>

    suspend fun updateRentalStatus(
        rentalId: String,
        status: RentalStatus
    )

    suspend fun makeOffer(
        rentalId: String,
        newPrice: Long
    )

}