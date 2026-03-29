package com.arnoagape.lokavelo.data.service.rental

import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.snapshots
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val RENTALS_COLLECTION = "rentals"
private const val USERS_COLLECTION = "users"

class FirebaseRentalApi @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : RentalApi {

    private val rentalsCollection by lazy { firestore.collection(RENTALS_COLLECTION) }
    private val usersCollection by lazy { firestore.collection(USERS_COLLECTION)}

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    // ─────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────

    override suspend fun createRental(rental: Rental) {
        rentalsCollection.document(rental.id).set(rental).await()
    }

    override suspend fun markRentalsAsRead(ownerId: String) {
        usersCollection
            .document(ownerId)
            .update("pendingRentalsUnread", 0)
            .await()
    }

    override suspend fun updateRentalStatus(rentalId: String, status: RentalStatus) {
        rentalsCollection.document(rentalId)
            .update("status", status.name)
            .await()
    }

    override suspend fun makeOffer(rentalId: String, newPrice: Long) {
        val serviceFee = (newPrice * SERVICE_FEE_RATE).toLong()
        val totalPrice = newPrice + serviceFee
        rentalsCollection.document(rentalId)
            .update(
                mapOf(
                    "basePriceInCents"   to newPrice,
                    "serviceFeeInCents"  to serviceFee,
                    "priceTotalInCents"  to totalPrice,
                    "status"             to RentalStatus.COUNTER_OFFER.name
                )
            )
            .await()
    }

    // ─────────────────────────────────────────────
    // Observe
    // ─────────────────────────────────────────────

    override fun observeOwnerRentals(): Flow<List<Rental>> = flow {
        val userId = requireUserId()
        emitAll(
            rentalsCollection
                .whereEqualTo("ownerId", userId)
                .snapshots()
                .map { it.documents.toRentalList() }
                .flowOn(Dispatchers.IO)
        )
    }

    override fun observeUserRentals(): Flow<List<Rental>> = flow {
        val userId = requireUserId()
        emitAll(
            rentalsCollection
                .whereEqualTo("renterId", userId)
                .snapshots()
                .map { it.documents.toRentalList() }
                .flowOn(Dispatchers.IO)
        )
    }

    override fun observeRental(conversationId: String): Flow<Rental?> =
        rentalsCollection
            .document(conversationId)
            .snapshots()
            .map { it.toObject(Rental::class.java)?.copy(id = it.id) }
            .flowOn(Dispatchers.IO)
}

// ─────────────────────────────────────────────
// Extensions
// ─────────────────────────────────────────────

private fun List<DocumentSnapshot>.toRentalList(): List<Rental> =
    mapNotNull { doc -> doc.toObject(Rental::class.java)?.copy(id = doc.id) }