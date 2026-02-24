package com.arnoagape.lokavelo.data.service.bike

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.arnoagape.lokavelo.data.compression.ImageCompressor
import com.arnoagape.lokavelo.data.dto.BikeDto
import com.arnoagape.lokavelo.domain.model.Bike
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FirebaseBikeApi @Inject constructor(
    private val compressor: ImageCompressor,
    @param:ApplicationContext private val context: Context
) : BikeApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private fun bikesCollectionForUser(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("bikes")

    override fun observeBikesForOwner(ownerId: String): Flow<List<Bike>> {

        return bikesCollectionForUser(ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<BikeDto>()
            .map { it.map(Bike::fromDto) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun addBike(localUris: List<Uri>, bike: Bike): List<String> {

        require(localUris.size <= 3) {
            "Maximum 3 photos allowed"
        }

        val ownerId = requireUserId()
        val bikeRef = bikesCollectionForUser(ownerId).document()
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

    override suspend fun editBike(
        localUris: List<Uri>,
        bike: Bike
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {
            val ownerId = requireUserId()
            val bikeId = bike.id

            val docRef = bikesCollectionForUser(ownerId).document(bikeId)

            // 1️⃣ Récupérer état actuel Firestore
            val snapshot = docRef.get().await()
            val existingBike = snapshot.toObject(BikeDto::class.java)
                ?.let { Bike.fromDto(it) }
                ?: return@withContext Result.failure(
                    IllegalStateException("Bike not found")
                )

            val oldUrls = existingBike.photoUrls

            // 2️⃣ URLs à supprimer
            val deletedUrls = oldUrls - bike.photoUrls.toSet()

            deleteBikePicturesByUrls(deletedUrls)

            // 3️⃣ Upload nouvelles photos
            val uploadedUrls = if (localUris.isNotEmpty()) {
                uploadBikePictures(ownerId, bikeId, localUris)
            } else emptyList()

            // 4️⃣ Liste finale propre
            val finalUrls = bike.photoUrls + uploadedUrls

            // 5️⃣ Mise à jour Firestore
            docRef.update(
                mapOf(
                    "title" to bike.title,
                    "description" to bike.description,
                    "location" to bike.location,
                    "priceInCents" to bike.priceInCents,
                    "priceHalfDayInCents" to bike.priceHalfDayInCents,
                    "priceWeekInCents" to bike.priceWeekInCents,
                    "priceMonthInCents" to bike.priceMonthInCents,
                    "depositInCents" to bike.depositInCents,
                    "electric" to bike.electric,
                    "category" to bike.category,
                    "brand" to bike.brand,
                    "condition" to bike.condition,
                    "accessories" to bike.accessories,
                    "photoUrls" to finalUrls
                )
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("FirebaseBikeApi", "Edit failed", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves a single medicine by its identifier.
     *
     * Data collection and mapping are executed on an IO thread.
     */
    override fun getBikeById(bikeId: String, userId: String): Flow<Bike> {
        return bikesCollectionForUser(userId)
            .document(bikeId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(BikeDto::class.java)
                    ?.let { Bike.fromDto(it) }
                    ?: throw NoSuchElementException("Bike $bikeId not found")
            }
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
                val ownerId = requireUserId()
                ids.forEach { id ->
                    if (id.isBlank()) error("Bike ID empty")
                    bikesCollectionForUser(ownerId).document(id).delete()
                }
                ids.forEach { id ->
                    deleteBikeFolder(ownerId, id)
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

                // 🔥 1️⃣ Normalisation EXIF
                val normalizedUri = normalizeImage(context, uri)

                // 🔥 2️⃣ Compression
                val compressedFile = compressor.compress(normalizedUri)

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

    private suspend fun deleteBikePicturesByUrls(urls: List<String>) {
        urls.forEach { url ->
            try {
                val ref = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(url)

                ref.delete().await()
            } catch (e: Exception) {
                Log.e("FirebaseDelete", "Error deleting photo: $url", e)
            }
        }
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

}

fun normalizeImage(
    context: Context,
    uri: Uri
): Uri {

    val original = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IllegalStateException("Decode failed")

    val orientation = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }

    val corrected = Bitmap.createBitmap(
        original,
        0,
        0,
        original.width,
        original.height,
        matrix,
        true
    )

    if (corrected != original) {
        original.recycle()
    }

    val file = File(
        context.cacheDir,
        "normalized_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use {
        corrected.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }

    corrected.recycle()

    return file.toUri()
}