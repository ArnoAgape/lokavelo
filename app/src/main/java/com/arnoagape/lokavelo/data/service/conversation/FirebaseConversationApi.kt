package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.utils.ConversationIdBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import kotlin.jvm.java

class FirebaseConversationApi : ConversationApi {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getOrCreateConversation(
        bikeId: String,
        ownerId: String,
        renterId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Conversation {

        val conversationId = ConversationIdBuilder.build(
            bikeId,
            ownerId,
            renterId,
            startDate.toEpochDay(),
            endDate.toEpochDay()
        )

        val ref = firestore.collection("conversations").document(conversationId)

        val snapshot = ref.get().await()

        if (snapshot.exists()) {
            return snapshot.toObject(Conversation::class.java)
                ?: error("Conversation mapping failed")
        }


        val conversation = Conversation(
            id = conversationId,
            bikeId = bikeId,
            ownerId = ownerId,
            renterId = renterId,
            startDateEpochDay = startDate.toEpochDay(),
            endDateEpochDay = endDate.toEpochDay(),
            participants = listOf(ownerId, renterId),
            createdAt = System.currentTimeMillis()
        )

        ref.set(conversation).await()

        return conversation
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> {

        return firestore
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Message::class.java)
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun sendMessage(
        conversationId: String,
        message: Message
    ) {

        val conversationRef =
            firestore.collection("conversations").document(conversationId)

        val messageRef =
            conversationRef
                .collection("messages")
                .document()

        val msg = message.copy(id = messageRef.id)

        firestore.runBatch { batch ->

            batch.set(messageRef, msg)

            batch.update(
                conversationRef,
                mapOf(
                    "lastMessage" to msg.text,
                    "lastMessageTime" to msg.createdAt,
                    "lastSenderId" to msg.senderId
                )
            )
        }.await()
    }

    override fun observeUserConversations(userId: String): Flow<List<Conversation>> {

        return firestore
            .collection("conversations")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(Conversation::class.java) }
    }
}