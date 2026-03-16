package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ConversationApi {

    suspend fun getOrCreateConversation(
        bikeId: String,
        ownerId: String,
        ownerName: String,
        renterId: String,
        renterName: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Conversation

    fun observeConversation(conversationId: String): Flow<Conversation?>

    fun observeMessages(conversationId: String): Flow<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        message: Message,
        receiverId: String
    )

    fun observeUserConversations(userId: String): Flow<List<Conversation>>

    fun observeUnreadCount(userId: String): Flow<Int>

    suspend fun markConversationAsRead(conversationId: String, userId: String)

    suspend fun setConversationActive(conversationId: String, userId: String)

    suspend fun clearConversationActive(userId: String)

}