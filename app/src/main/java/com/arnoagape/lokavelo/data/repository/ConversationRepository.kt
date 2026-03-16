package com.arnoagape.lokavelo.data.repository

import com.arnoagape.lokavelo.data.service.conversation.ConversationApi
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages conversation-related operations.
 * Delegates data access to [ConversationApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationRepository @Inject constructor(
    private val conversationApi: ConversationApi
) {

    suspend fun getOrCreateConversation(
        bikeId: String, ownerId: String, ownerName: String, renterId: String, renterName: String,
        startDate: LocalDate, endDate: LocalDate
    ) = conversationApi.getOrCreateConversation(
        bikeId,
        ownerId,
        ownerName,
        renterId,
        renterName,
        startDate,
        endDate
    )

    fun observeConversation(conversationId: String): Flow<Conversation?> =
        conversationApi.observeConversation(conversationId)

    fun observeMessages(conversationId: String): Flow<List<Message>> =
        conversationApi.observeMessages(conversationId)

    suspend fun sendMessage(conversationId: String, message: Message, receiverId: String) =
        conversationApi.sendMessage(conversationId, message, receiverId)

    fun observeUserConversations(userId: String): Flow<List<Conversation>> =
        conversationApi.observeUserConversations(userId)

    fun observeUnreadCount(userId: String): Flow<Int> = conversationApi.observeUnreadCount(userId)

    suspend fun markConversationAsRead(conversationId: String, userId: String) =
        conversationApi.markConversationAsRead(conversationId, userId)

    suspend fun setConversationActive(conversationId: String, userId: String) =
        conversationApi.setConversationActive(conversationId, userId)

    suspend fun clearConversationActive(userId: String) =
        conversationApi.clearConversationActive(userId)
}