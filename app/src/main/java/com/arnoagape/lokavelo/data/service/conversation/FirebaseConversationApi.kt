package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.utils.ConversationIdBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
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

        val ref = firestore.collection("conversations").document()

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
            startDateMillis = startDate.toEpochDay(),
            endDateMillis = endDate.toEpochDay()
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
    }

    override suspend fun sendMessage(
        conversationId: String,
        message: Message
    ) {

        val ref = firestore
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document()

        ref.set(message.copy(id = ref.id)).await()
    }
}