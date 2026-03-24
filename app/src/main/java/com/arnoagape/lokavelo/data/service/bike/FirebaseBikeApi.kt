package com.arnoagape.lokavelo.data.service.bike

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.arnoagape.lokavelo.data.compression.ImageCompressor
import com.arnoagape.lokavelo.data.dto.BikeDto
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.utils.normalizeImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "FirebaseBikeApi"
private const val BIKES_COLLECTION = "bikes"
private const val MAX_PHOTOS = 3
private const val FIRESTORE_IN_CLAUSE_LIMIT = 10

class FirebaseBikeApi @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val compressor: ImageCompressor,
    @param:ApplicationContext private val context: Context
) : BikeApi {

    private val bikesCollection by lazy { firestore.collection(BIKES_COLLECTION) }

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

    private val currentOwnerName: String
        get() = auth.currentUser?.displayName ?: "Utilisateur"

    // ─────────────────────────────────────────────
    // Observe
    // ─────────────────────────────────────────────

    override fun observeBikesByIds(ids: List<String>): Flow<List<Bike>> {
        if (ids.isEmpty()) return flowOf(emptyList())

        val flows = ids
            .chunked(FIRESTORE_IN_CLAUSE_LIMIT)
            .map { chunk ->
                firestore.collection(BIKES_COLLECTION)
                    .whereIn(FieldPath.documentId(), chunk)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(BikeDto::class.java)
                                ?.let { Bike.fromDto(it).copy(id = doc.id) }
                        }
                    }
            }

        return combine(flows) { results ->
            results.asList().flatten().distinctBy { it.id }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeBikesForOwner(ownerId: String): Flow<List<Bike>> =
        bikesCollection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<BikeDto>()
            .map { dtos -> dtos.map(Bike::fromDto) }
            .flowOn(Dispatchers.IO)

    override fun observeAllBikes(): Flow<List<Bike>> =
        bikesCollection
            .whereEqualTo("available", true)
            .dataObjects<BikeDto>()
            .map { dtos -> dtos.map(Bike::fromDto) }
            .flowOn(Dispatchers.IO)

    override fun observeBikeForPublic(bikeId: String): Flow<Bike?> =
        bikesCollection
            .document(bikeId)
            .snapshots()
            .map { snapshot -> snapshot.toObject(BikeDto::class.java)?.let(Bike::fromDto) }
            .flowOn(Dispatchers.IO)

    override fun getBikeById(bikeId: String): Flow<Bike?> =
        bikesCollection
            .document(bikeId)
            .snapshots()
            .map { snapshot -> snapshot.toObject(BikeDto::class.java)?.let(Bike::fromDto) }
            .flowOn(Dispatchers.IO)

    // ─────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────

    override suspend fun addBike(localUris: List<Uri>, bike: Bike): List<String> {
        require(localUris.size <= MAX_PHOTOS) { "Maximum $MAX_PHOTOS photos allowed" }

        val ownerId = requireUserId()
        val bikeRef = bikesCollection.document()

        val uploadedUrls = uploadBikePictures(
            ownerId = ownerId,
            bikeId = bikeRef.id,
            uris = localUris
        )

        val finalBike = bike.copy(
            id = bikeRef.id,
            ownerId = ownerId,
            ownerName = currentOwnerName,
            photoUrls = uploadedUrls
        )

        bikeRef.set(finalBike.toDto()).await()

        return uploadedUrls
    }

    override suspend fun editBike(
        localUris: List<Uri>,
        bike: Bike
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val docRef = bikesCollection.document(bike.id)

            val existingBike = docRef.get().await()
                .toObject(BikeDto::class.java)
                ?.let(Bike::fromDto)
                ?: error("Bike ${bike.id} not found")

            // Supprimer les photos retirées
            val deletedUrls = existingBike.photoUrls - bike.photoUrls.toSet()
            deleteBikePicturesByUrls(deletedUrls)

            // Uploader les nouvelles photos locales
            val uploadedUrls = if (localUris.isNotEmpty()) {
                uploadBikePictures(requireUserId(), bike.id, localUris)
            } else emptyList()

            val finalUrls = bike.photoUrls + uploadedUrls

            docRef.update(bike.toUpdateMap(currentOwnerName, finalUrls)).await()
            Unit
        }.onFailure { Log.e(TAG, "editBike failed", it) }
    }

    // ─────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────

    override suspend fun deleteBikes(ids: Set<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val ownerId = requireUserId()

                ids.forEach { id ->
                    require(id.isNotBlank()) { "Bike ID must not be blank" }
                    bikesCollection.document(id).delete().await()
                }

                ids.forEach { id -> deleteBikeFolder(ownerId, id) }
            }.onFailure { Log.e(TAG, "deleteBikes failed", it) }
        }

    // ─────────────────────────────────────────────
    // Updates availability
    // ─────────────────────────────────────────────

    override suspend fun updateAvailability(id: String, available: Boolean): Result<Unit> {
        return try {
            bikesCollection.document(id).update("available", available).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────
    // Storage helpers
    // ─────────────────────────────────────────────

    private suspend fun uploadBikePictures(
        ownerId: String,
        bikeId: String,
        uris: List<Uri>
    ): List<String> = uris.mapNotNull { uri ->
        runCatching {
            val normalizedUri = normalizeImage(context, uri)
            val compressedFile = compressor.compress(normalizedUri)
            val fileName = "${UUID.randomUUID()}.jpg"

            val ref = storage.reference
                .child("bikePictures")
                .child("user_$ownerId")
                .child("bike_$bikeId")
                .child(fileName)

            ref.putFile(compressedFile.toUri()).await()
            ref.downloadUrl.await().toString()
        }.getOrElse {
            Log.e(TAG, "Upload failed for $uri", it)
            null
        }
    }

    private suspend fun deleteBikeFolder(ownerId: String, bikeId: String) {
        val folderRef = storage.reference
            .child("bikePictures")
            .child("user_$ownerId")
            .child("bike_$bikeId")

        folderRef.listAll().await().items.forEach { it.delete().await() }
    }

    private suspend fun deleteBikePicturesByUrls(urls: List<String>) {
        urls.forEach { url ->
            runCatching {
                storage.getReferenceFromUrl(url).delete().await()
            }.onFailure { Log.e(TAG, "Failed to delete photo: $url", it) }
        }
    }
}

// ─────────────────────────────────────────────
// Extension — mapping vers Firestore
// ─────────────────────────────────────────────

/**
 * Construit la map de mise à jour Firestore pour un vélo existant.
 */
private fun Bike.toUpdateMap(ownerName: String, photoUrls: List<String>): Map<String, Any?> =
    mapOf(
        "title" to title,
        "description" to description,
        "ownerName" to ownerName,
        "ownerId" to ownerId,
        "location" to location,
        "priceInCents" to priceInCents,
        "priceTwoDaysInCents" to priceTwoDaysInCents,
        "priceWeekInCents" to priceWeekInCents,
        "priceMonthInCents" to priceMonthInCents,
        "depositInCents" to depositInCents,
        "electric" to electric,
        "category" to category,
        "brand" to brand,
        "size" to size,
        "condition" to condition,
        "accessories" to accessories,
        "photoUrls" to photoUrls,
        "available" to available,
        "minDaysRental" to minDaysRental
    )