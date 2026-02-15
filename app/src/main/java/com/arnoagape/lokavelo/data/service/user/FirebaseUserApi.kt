package com.arnoagape.lokavelo.data.service.user

import android.util.Log
import com.arnoagape.lokavelo.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firebase-based implementation of [UserApi].
 *
 * Handles authentication state and user persistence using
 * Firebase Authentication and Firestore.
 */
class FirebaseUserApi : UserApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private fun FirebaseUser.toDomain(): User = User(
        id = uid,
        displayName = displayName,
        photoUrl = photoUrl.toString(),
        email = email,
        phoneNumber = phoneNumber
    )

    /**
     * Returns the currently authenticated user if available.
     *
     * This is a memory read and does not require a background thread.
     */
    override suspend fun getCurrentUser(): User? = auth.currentUser?.toDomain()

    /**
     * Observes authentication state changes in real time.
     *
     * Emits the current user or null when signed out.
     */
    override fun observeCurrentUser(): Flow<User?> = callbackFlow {

        trySend(auth.currentUser?.toDomain())

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Updates the authenticated user's profile and persists
     * additional fields in Firestore.
     *
     * Network operations are executed on an IO thread.
     */
    override suspend fun updateUser(user: User): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                    ?: return@withContext Result.failure(Exception("User not signed in"))

                val profileUpdates = userProfileChangeRequest {
                    displayName = user.displayName
                }
                currentUser.updateProfile(profileUpdates)

                user.email?.let { email ->
                    if (email != currentUser.email) {
                        currentUser.verifyBeforeUpdateEmail(email)
                    }
                }

                usersCollection.document(currentUser.uid)
                    .set(user.toDto(), SetOptions.merge())

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Ensures the authenticated user exists in Firestore.
     *
     * Creates the document if it does not already exist.
     */
    override suspend fun ensureUserInFirestore(): Result<Unit> =
        withContext(Dispatchers.IO) {
            val firebaseUser = auth.currentUser
                ?: return@withContext Result.failure(Exception("User not signed in"))
            val user = firebaseUser.toDomain()
            try {
                val doc = usersCollection.document(user.id).get().await()
                if (!doc.exists()) {
                    usersCollection.document(user.id).set(user).await()
                    Log.d("UserRepository", "Document Firestore created for ${user.email}")
                } else {
                    Log.d("UserRepository", "Document Firestore already exists for ${user.email}")
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Signs the current user out of Firebase Authentication.
     */
    override fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


    /**
     * Observes whether a user is currently authenticated.
     */
    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {

        trySend(auth.currentUser != null)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }

        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Deletes the authenticated user and their Firestore document.
     *
     * Executed on an IO thread due to network operations.
     */
    override suspend fun deleteUser(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                    ?: return@withContext Result.failure(Exception("No user signed in"))
                usersCollection.document(currentUser.uid).delete().await()
                currentUser.delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}