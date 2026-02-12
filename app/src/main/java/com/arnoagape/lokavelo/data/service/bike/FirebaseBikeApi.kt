package com.arnoagape.lokavelo.data.service.bike

import android.util.Log
import com.arnoagape.lokavelo.data.dto.BikeDto
import com.arnoagape.lokavelo.domain.model.Bike
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FirebaseBikeApi @Inject constructor() : BikeApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val bikesCollection = firestore.collection("bikes")

    override fun getAllBikes(): Flow<List<Bike>> {

        return bikesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<BikeDto>()
            .map { it.map(Bike::fromDto) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun addBike(bike: Bike): Result<Bike> =
        withContext(Dispatchers.IO) {
            try {
                val docRef = bikesCollection.document()
                val savedMedicine = bike.copy(id = docRef.id)
                docRef.set(savedMedicine.toDto())
                Result.success(savedMedicine)

            } catch (e: Exception) {
                Log.e("FirebaseBikeApi", "Error while adding medicine", e)
                Result.failure(e)
            }
        }

    override suspend fun editBike(bike: Bike): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val dto = bike.toDto()

                bikesCollection
                    .document(bike.id)
                    .set(dto)

                Result.success(Unit)

            } catch (e: Exception) {
                Log.e("FirebaseBikeApi", "Error while editing bike", e)
                Result.failure(e)
            }
        }

    /**
     * Retrieves a single medicine by its identifier.
     *
     * Data collection and mapping are executed on an IO thread.
     */
    override fun getBikeById(bikeId: String): Flow<Bike> {
        return bikesCollection
            .whereEqualTo("id", bikeId)
            .limit(1)
            .dataObjects<BikeDto>()
            .map { Bike.fromDto(it.first()) }
            .flowOn(Dispatchers.IO)
    }

    /*override fun getBikesByCategory(
        category: BikeCategory,
        categoryId: String
    ): Flow<List<Bike>> {

        bikesCollection
            .whereEqualTo("categoryId", categoryId)
            .orderBy("nameLowercase")
        return
            .dataObjects<BikeDto>()
            .map { dto ->
                sort.sort(dto.map { Bike.fromDto(it) })
            }
            .flowOn(Dispatchers.IO)
    }*/

    /**
     * Deletes multiple medicines from Firestore.
     *
     * Each deletion is a network operation executed on an IO thread.
     */
    override suspend fun deleteBikes(ids: Set<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                ids.forEach { id ->
                    if (id.isBlank()) error("Bike ID empty")
                    bikesCollection.document(id).delete()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}