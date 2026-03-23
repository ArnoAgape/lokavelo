package com.arnoagape.lokavelo.data.repository

import android.net.Uri
import com.arnoagape.lokavelo.data.service.bike.BikeApi
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages bike-related operations.
 * Delegates data access to [BikeApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class BikeRepository @Inject constructor(
    private val bikeApi: BikeApi,
    private val userRepository: UserRepository,
    private val rentalRepository: RentalRepository
) {

    fun observeBikesByIds(ids: List<String>): Flow<List<Bike>> = bikeApi.observeBikesByIds(ids)

    fun observeOwnerBikesWithRentals(): Flow<List<BikeWithRentals>> =
        userRepository.observeCurrentUser()
            .filterNotNull()
            .map { it.id }
            .distinctUntilChanged()
            .flatMapLatest { ownerId ->

                combine(
                    bikeApi.observeBikesForOwner(ownerId),
                    rentalRepository.observeOwnerRentals()
                ) { bikes, rentals ->

                    bikes.map { bike ->
                        BikeWithRentals(
                            bike = bike,
                            rentals = rentals.filter { it.bikeId == bike.id }
                        )
                    }
                }
            }

    fun observePublicBikes(): Flow<List<Bike>> = bikeApi.observeAllBikes()

    fun observeOwnerBike(bikeId: String): Flow<Bike?> =
        userRepository.observeCurrentUser()
            .filterNotNull()
            .map { it.id }
            .distinctUntilChanged()
            .flatMapLatest { bikeApi.getBikeById(bikeId) }

    fun observeBike(bikeId: String): Flow<Bike?> = bikeApi.observeBikeForPublic(bikeId)

    suspend fun editBike(localUris: List<Uri>, bike: Bike) = bikeApi.editBike(localUris, bike)

    suspend fun deleteBikes(ids: Set<String>) = bikeApi.deleteBikes(ids)

    suspend fun addBike(localUris: List<Uri>, bike: Bike) = bikeApi.addBike(localUris, bike)
}