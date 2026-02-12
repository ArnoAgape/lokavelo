package com.arnoagape.lokavelo.data.service.bike

import com.arnoagape.lokavelo.domain.model.Bike
import kotlinx.coroutines.flow.Flow

interface BikeApi {

    fun getAllBikes(): Flow<List<Bike>>
    suspend fun addBike(bike: Bike): Result<Bike>
    suspend fun editBike(bike: Bike): Result<Unit>
    fun getBikeById(bikeId: String): Flow<Bike>
    suspend fun deleteBikes(ids: Set<String>): Result<Unit>
    // fun getBikesByCategory(category: BikeCategory, categoryId: String): Flow<List<Bike>>
}