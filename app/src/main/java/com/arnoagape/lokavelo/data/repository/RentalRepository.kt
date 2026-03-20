package com.arnoagape.lokavelo.data.repository

import com.arnoagape.lokavelo.data.service.rental.RentalApi
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    suspend fun markRentalsAsRead(ownerId: String) = rentalApi.markRentalsAsRead(ownerId)
    fun observeOwnerRentals(): Flow<List<Rental>> = rentalApi.observeOwnerRentals()

    private fun observeUserRentals(): Flow<List<Rental>> = rentalApi.observeUserRentals()

    fun observeAllMyRentals(): Flow<List<Rental>> {
        return combine(
            observeOwnerRentals(),
            observeUserRentals()
        ) { owner, renter ->
            (owner + renter)
                .distinctBy { it.id } // sécurité anti doublon
        }
    }

    fun observeRental(conversationId: String): Flow<Rental?> =
        rentalApi.observeRental(conversationId)

    suspend fun updateRentalStatus(rentalId: String, status: RentalStatus) =
        rentalApi.updateRentalStatus(rentalId, status)

    suspend fun makeOffer(rentalId: String, newPrice: Long) =
        rentalApi.makeOffer(rentalId, newPrice)
}