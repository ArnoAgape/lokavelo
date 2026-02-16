package com.arnoagape.lokavelo.data.service.bike

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.arnoagape.lokavelo.data.compression.ImageCompressor
import com.arnoagape.lokavelo.data.dto.BikeDto
import com.arnoagape.lokavelo.domain.model.Bike
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.storage.FirebaseStorage
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseBikeApi @Inject constructor(
    private val compressor: ImageCompressor
) : BikeApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val bikesCollection = firestore.collection("bikes")

    override fun observeBikesForOwner(ownerId: String): Flow<List<Bike>> {

        return bikesCollection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<BikeDto>()
            .map { it.map(Bike::fromDto) }
            .flowOn(Dispatchers.IO)
            .catch { e ->
                Log.e("FIRESTORE", "Flow error", e)
                throw e
            }
    }

    override suspend fun addBike(localUris: List<Uri>, bike: Bike): List<String> {

        require(localUris.size <= 3) {
            "Maximum 3 photos allowed"
        }

        val ownerId = requireUserId()

        val bikeRef = bikesCollection.document()
        val bikeId = bikeRef.id

        val uploadedUrls = uploadBikePictures(
            ownerId = ownerId,
            bikeId = bikeId,
            uris = localUris
        )

        val finalBike = bike.copy(
            id = bikeId,
            ownerId = ownerId,
            photoUrls = uploadedUrls
        )
        Log.d("SAVE", "Full DTO = $finalBike")

        bikeRef.set(finalBike.toDto()).await()

        return uploadedUrls
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
    override fun getBikeById(bikeId: String, userId: String): Flow<Bike> {
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

    /**
     * Uploads a bike photo to Firebase Storage.
     * Validates MIME type and returns the public download URL.
     */
    private suspend fun uploadBikePictures(
        ownerId: String,
        bikeId: String,
        uris: List<Uri>
    ): List<String> {

        return uris.mapNotNull { uri ->

            try {

                val compressedFile = compressor.compress(uri)

                val fileName = "${UUID.randomUUID()}.jpg"

                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("bikePictures")
                    .child("user_$ownerId")
                    .child("bike_$bikeId")
                    .child(fileName)

                storageRef.putFile(compressedFile.toUri()).await()

                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading", e)
                null
            }
        }
    }

    /**
     * Deletes a photo from Firebase Storage.
     * Validates MIME type and returns the public download URL.
     */
    private suspend fun deleteBikeFolder(ownerId: String, bikeId: String) {
        val folderRef = FirebaseStorage.getInstance()
            .reference
            .child("bikePictures")
            .child("user_$ownerId")
            .child("bike_$bikeId")

        val list = folderRef.listAll().await()

        list.items.forEach { it.delete().await() }
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

}