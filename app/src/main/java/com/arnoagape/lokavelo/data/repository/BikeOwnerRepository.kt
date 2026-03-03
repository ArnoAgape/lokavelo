package com.arnoagape.lokavelo.data.repository

import android.net.Uri
import android.util.Log
import com.arnoagape.lokavelo.data.service.bike.BikeApi
import com.arnoagape.lokavelo.domain.model.Bike
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages file-related operations.
 * Delegates data access to [BikeApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class BikeOwnerRepository @Inject constructor(
    private val bikeApi: BikeApi,
    private val userRepository: UserRepository
) {

    fun observeBikesForOwner(): Flow<List<Bike>> =
        userRepository.observeCurrentUser()
            .filterNotNull()
            .map { it.id }
            .distinctUntilChanged()
            .flatMapLatest { ownerId ->
                bikeApi.observeBikesForOwner(ownerId)
            }
            .onEach { bikes ->
                Log.d("BikesFlow", "Received ${bikes.size} bikes")
            }
            .catch { e ->
                Log.e("BikesFlow", "Error in observeBikes", e)
                throw e
            }

    fun observeAllBikes(): Flow<List<Bike>> = bikeApi.observeAllBikes()

    fun observeBike(bikeId: String): Flow<Bike?> =
        userRepository.observeCurrentUser()
            .filterNotNull()
            .map { it.id }
            .distinctUntilChanged()
            .flatMapLatest { ownerId ->
                bikeApi.getBikeById(bikeId, ownerId)
            }

    suspend fun editBike(localUris: List<Uri>, bike: Bike) = bikeApi.editBike(localUris, bike)

    suspend fun deleteBikes(ids: Set<String>) = bikeApi.deleteBikes(ids)

    suspend fun addBike(localUris: List<Uri>, bike: Bike) = bikeApi.addBike(localUris, bike)
}