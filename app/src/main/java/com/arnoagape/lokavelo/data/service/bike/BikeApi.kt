package com.arnoagape.lokavelo.data.service.bike

import android.net.Uri
import com.arnoagape.lokavelo.domain.model.Bike
import kotlinx.coroutines.flow.Flow

interface BikeApi {

    fun observeBikesByIds(ids: List<String>): Flow<List<Bike>>
    fun observeBikesForOwner(ownerId: String): Flow<List<Bike>>
    fun observeAllBikes(): Flow<List<Bike>>
    fun observeBikeForPublic(bikeId: String): Flow<Bike?>
    suspend fun addBike(localUris: List<Uri>, bike: Bike): List<String>
    suspend fun editBike(localUris: List<Uri>, bike: Bike): Result<Unit>
    fun getBikeById(bikeId: String): Flow<Bike?>
    suspend fun deleteBikes(ids: Set<String>): Result<Unit>
    // fun getBikesByCategory(category: BikeCategory, categoryId: String): Flow<List<Bike>>
}