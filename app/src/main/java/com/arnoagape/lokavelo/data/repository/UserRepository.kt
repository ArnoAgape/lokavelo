package com.arnoagape.lokavelo.data.repository

import com.arnoagape.lokavelo.data.service.user.UserApi
import com.arnoagape.lokavelo.domain.model.User
import com.arnoagape.lokavelo.ui.preview.PreviewData.user
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * This class provides a repository for accessing and managing User data.
 * It utilizes dependency injection to retrieve a UserApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {
    fun observeUser(userId: String): Flow<User?> = userApi.observeUser(userId)
    fun observePendingRentalsUnread(userId: String): Flow<Int> =
        userApi.observePendingRentalsUnread(userId)
    suspend fun markRentalsAsRead(userId: String) =
        userApi.markRentalsAsRead(userId)
    suspend fun incrementPendingRentalsUnread(ownerId: String) =
        userApi.incrementPendingRentalsUnread(ownerId)
    suspend fun getUser(userId: String): User? = userApi.getUser(userId)
    fun observeCurrentUser(): Flow<User?> = userApi.observeCurrentUser()
    suspend fun updateUser(user: User) = userApi.updateUser(user)
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()
    fun signOut() = userApi.signOut()
    fun isUserSignedIn(): Flow<Boolean> = userApi.isUserSignedIn()
    suspend fun deleteUser() = userApi.deleteUser()
    suspend fun saveFcmToken() = userApi.saveFcmToken()
}