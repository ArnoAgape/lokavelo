package com.arnoagape.lokavelo.data.service.user

import com.arnoagape.lokavelo.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for interacting with user data and authentication state.
 * This interface abstracts away the details of user management such as
 * authentication, Firestore persistence, and observing sign-in status.
 */
interface UserApi {

    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun ensureUserInFirestore(): Result<Unit>
    fun signOut(): Result<Unit>
    fun isUserSignedIn(): Flow<Boolean>
    suspend fun deleteUser(): Result<Unit>
}