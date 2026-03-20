package com.arnoagape.lokavelo.data.service.user

import android.util.Log
import com.arnoagape.lokavelo.data.dto.UserDto
import com.arnoagape.lokavelo.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "FirebaseUserApi"
private const val USERS_COLLECTION = "users"

/**
 * Firebase-based implementation of [UserApi].
 *
 * Handles authentication state and user persistence using
 * Firebase Authentication and Firestore.
 */
class FirebaseUserApi @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : UserApi {

    private val usersCollection by lazy { firestore.collection(USERS_COLLECTION) }

    private fun FirebaseUser.toDomain(): User = User(
        id = uid,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        email = email,
        phoneNumber = phoneNumber
    )

    private fun requireCurrentUser() =
        auth.currentUser ?: throw IllegalStateException("User not signed in")

    // ─────────────────────────────────────────────
    // Auth
    // ─────────────────────────────────────────────

    /**
     * Returns the currently authenticated user if available.
     * Memory read — no background thread needed.
     */
    override suspend fun getCurrentUser(): User? = auth.currentUser?.toDomain()

    /**
     * Observes authentication state changes in real time.
     * Emits the current user or null when signed out.
     */
    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        trySend(auth.currentUser?.toDomain())
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Observes whether a user is currently authenticated. */
    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        trySend(auth.currentUser != null)
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Signs the current user out of Firebase Authentication. */
    override fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    // ─────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────

    override fun observeUser(userId: String): Flow<User?> =
        usersCollection
            .document(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserDto::class.java)
                    ?.let { User.fromDto(it.copy(id = snapshot.id)) }
            }
            .flowOn(Dispatchers.IO)

    override suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        usersCollection.document(userId).get().await()
            .takeIf { it.exists() }
            ?.toObject(UserDto::class.java)
            ?.let { User.fromDto(it.copy(id = userId)) }
    }

    override fun observePendingRentalsUnread(userId: String): Flow<Int> =
        callbackFlow {
            val listener = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    val count = snapshot?.getLong("pendingRentalsUnread")?.toInt() ?: 0
                    trySend(count)
                }
            awaitClose { listener.remove() }
        }

    // ─────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────

    override suspend fun saveFcmToken(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = messaging.token.await()
            usersCollection
                .document(requireCurrentUser().uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
            Unit
        }
    }

    override suspend fun markRentalsAsRead(userId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update("pendingRentalsUnread", 0)
            .await()
    }

    override suspend fun incrementPendingRentalsUnread(ownerId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(ownerId)
            .set(
                mapOf("pendingRentalsUnread" to FieldValue.increment(1)),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Updates the authenticated user's profile and persists
     * additional fields in Firestore.
     */
    override suspend fun updateUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val currentUser = requireCurrentUser()

            currentUser.updateProfile(
                userProfileChangeRequest { displayName = user.displayName }
            ).await()

            user.email?.takeIf { it != currentUser.email }
                ?.let { currentUser.verifyBeforeUpdateEmail(it).await() }

            usersCollection.document(currentUser.uid)
                .set(user.toDto(), SetOptions.merge())
                .await()
            Unit
        }
    }

    /**
     * Ensures the authenticated user exists in Firestore.
     * Creates the document if it does not already exist.
     */
    override suspend fun ensureUserInFirestore(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = requireCurrentUser().toDomain()
            val doc = usersCollection.document(user.id).get().await()
            if (!doc.exists()) {
                usersCollection.document(user.id).set(user).await()
                Log.d(TAG, "Firestore document created for ${user.email}")
            } else {
                Log.d(TAG, "Firestore document already exists for ${user.email}")
            }
            Unit
        }
    }

    /**
     * Deletes the authenticated user and their Firestore document.
     */
    override suspend fun deleteUser(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val currentUser = requireCurrentUser()
            usersCollection.document(currentUser.uid).delete().await()
            currentUser.delete().await()
            Unit
        }
    }
}