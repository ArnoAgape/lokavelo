package com.arnoagape.lokavelo.data.service.rental

import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRentalApi : RentalApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun createRental(rental: Rental) {

        firestore
            .collection("rentals")
            .document(rental.id) // conversationId
            .set(rental)
            .await()
    }

    override fun observeOwnerRentals(): Flow<List<Rental>> {

        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        return firestore
            .collection("rentals")
            .whereEqualTo("ownerId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Rental::class.java)?.copy(id = doc.id)
                }
            }
    }

    override fun observeUserRentals(): Flow<List<Rental>> {

        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        return firestore
            .collection("rentals")
            .whereEqualTo("renterId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Rental::class.java)?.copy(id = doc.id)
                }
            }
    }

    override fun observeRental(conversationId: String): Flow<Rental?> {

        return firestore
            .collection("rentals")
            .document(conversationId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(Rental::class.java)?.copy(id = snapshot.id)
            }
    }

    override suspend fun updateRentalStatus(
        rentalId: String,
        status: RentalStatus
    ) {

        firestore
            .collection("rentals")
            .document(rentalId)
            .update("status", status.name)
            .await()
    }

    override suspend fun makeOffer(
        rentalId: String,
        newPrice: Long
    ) {

        firestore
            .collection("rentals")
            .document(rentalId)
            .update(
                mapOf(
                    "priceTotalInCents" to newPrice,
                    "status" to RentalStatus.COUNTER_OFFER.name
                )
            )
            .await()
    }
}