package com.arnoagape.lokavelo.data.repository

import com.arnoagape.lokavelo.data.service.rental.RentalApi
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages bike-related operations.
 * Delegates data access to [RentalApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class RentalRepository @Inject constructor(
    private val rentalApi: RentalApi
) {

    suspend fun createRental(rental: Rental) = rentalApi.createRental(rental)
    fun observeOwnerRentals(): Flow<List<Rental>> = rentalApi.observeOwnerRentals()

    fun observeRental(conversationId: String): Flow<Rental?> =
        rentalApi.observeRental(conversationId)

    suspend fun updateRentalStatus(rentalId: String, status: RentalStatus) =
        rentalApi.updateRentalStatus(rentalId, status)

    suspend fun makeOffer(rentalId: String, newPrice: Long) =
        rentalApi.makeOffer(rentalId, newPrice)
}