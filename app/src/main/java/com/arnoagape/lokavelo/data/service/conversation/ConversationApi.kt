package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ConversationApi {

    suspend fun getOrCreateConversation(
        bikeId: String,
        ownerId: String,
        renterId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Conversation

    fun observeMessages(conversationId: String): Flow<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        message: Message
    )

    fun observeUserConversations(userId: String): Flow<List<Conversation>>

}