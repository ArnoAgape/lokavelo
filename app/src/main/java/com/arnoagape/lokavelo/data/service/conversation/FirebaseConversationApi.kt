package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.utils.ConversationIdBuilder
import com.google.firebase.firestore.FieldValue
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
        ownerName: String,
        renterId: String,
        renterName: String,
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

        val now = System.currentTimeMillis()

        val conversation = Conversation(
            id = conversationId,
            bikeId = bikeId,
            ownerId = ownerId,
            ownerName = ownerName,
            renterId = renterId,
            renterName = renterName,
            startDateEpochDay = startDate.toEpochDay(),
            endDateEpochDay = endDate.toEpochDay(),
            participants = listOf(ownerId, renterId),
            createdAt = System.currentTimeMillis(),
            lastMessageTime = now
        )

        ref.set(conversation).await()

        return conversation
    }

    override fun observeConversation(conversationId: String): Flow<Conversation?> {
        return firestore
            .collection("conversations")
            .document(conversationId)
            .snapshots()
            .map { snapshot ->

                val conversation =
                    snapshot.toObject(Conversation::class.java)
                        ?: return@map null

                val unreadMap = snapshot.data?.extractUnreadMap() ?: emptyMap()

                conversation.copy(
                    id = snapshot.id,
                    unreadCount = unreadMap
                )
            }
            .catch { emit(null) }
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
        message: Message,
        receiverId: String
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
                    "lastSenderId" to msg.senderId,
                    "unread_$receiverId" to FieldValue.increment(1)
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
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->

                    val conversation = doc.toObject(Conversation::class.java)
                        ?: return@mapNotNull null

                    val unreadMap = doc.data?.extractUnreadMap() ?: emptyMap()

                    conversation.copy(
                        id = doc.id,
                        unreadCount = unreadMap
                    )
                }
            }
    }

    override fun observeUnreadCount(userId: String): Flow<Int> {

        return firestore.collection("conversations")
            .whereArrayContains("participants", userId)
            .snapshots()
            .map { snapshot ->

                snapshot.documents.sumOf { doc ->
                    doc.getLong("unread_$userId")?.toInt() ?: 0
                }
            }
    }

    override suspend fun markConversationAsRead(conversationId: String, userId: String) {
        firestore.collection("conversations")
            .document(conversationId)
            .update("unread_$userId", 0)
            .await()
    }

    override suspend fun setConversationActive(conversationId: String, userId: String) {
            firestore.collection("users")
                .document(userId)
                .update("activeConversationId", conversationId)
    }

    override suspend fun clearConversationActive(userId: String) {
            firestore.collection("users")
                .document(userId)
                .update("activeConversationId", FieldValue.delete())
    }
}

private fun Map<String, Any>.extractUnreadMap(): Map<String, Int> =
    filterKeys { it.startsWith("unread_") }
        .mapKeys { it.key.removePrefix("unread_") }
        .mapValues { (_, value) -> (value as? Number)?.toInt() ?: 0 }