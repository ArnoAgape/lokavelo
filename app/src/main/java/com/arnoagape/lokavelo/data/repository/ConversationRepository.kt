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
        bikeId: String, ownerId: String, renterId: String, startDate: LocalDate, endDate: LocalDate
    ) = conversationApi.getOrCreateConversation(
        bikeId,
        ownerId,
        renterId,
        startDate,
        endDate
    )

    fun observeMessages(conversationId: String): Flow<List<Message>> =
        conversationApi.observeMessages(conversationId)

    suspend fun sendMessage(conversationId: String, message: Message) =
        conversationApi.sendMessage(conversationId, message)

    fun observeUserConversations(userId: String): Flow<List<Conversation>> =
        conversationApi.observeUserConversations(userId)
}