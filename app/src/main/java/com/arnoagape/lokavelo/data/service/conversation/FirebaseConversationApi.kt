package com.arnoagape.lokavelo.data.service.conversation

import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.utils.ConversationIdBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

private const val CONVERSATIONS_COLLECTION = "conversations"
private const val MESSAGES_COLLECTION = "messages"
private const val USERS_COLLECTION = "users"

class FirebaseConversationApi @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConversationApi {

    private val conversationsCollection by lazy {
        firestore.collection(CONVERSATIONS_COLLECTION)
    }

    // ─────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────

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

        val ref = conversationsCollection.document(conversationId)
        val snapshot = ref.get().await()

        if (snapshot.exists()) {
            return snapshot.toObject(Conversation::class.java)
                ?: error("Conversation mapping failed for id=$conversationId")
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
            createdAt = now,
            lastMessageTime = now
        )

        ref.set(conversation).await()
        return conversation
    }

    override suspend fun sendMessage(
        conversationId: String,
        message: Message,
        receiverId: String
    ) {
        val conversationRef = conversationsCollection.document(conversationId)
        val messageRef = conversationRef.collection(MESSAGES_COLLECTION).document()
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

    override suspend fun markConversationAsRead(conversationId: String, userId: String) {
        conversationsCollection.document(conversationId)
            .update("unread_$userId", 0)
            .await()
    }

    override suspend fun setConversationActive(conversationId: String, userId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update("activeConversationId", conversationId)
            .await()
    }

    override suspend fun clearConversationActive(userId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update("activeConversationId", FieldValue.delete())
            .await()
    }

    // ─────────────────────────────────────────────
    // Observe
    // ─────────────────────────────────────────────

    override fun observeConversation(conversationId: String): Flow<Conversation?> =
        conversationsCollection
            .document(conversationId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(Conversation::class.java)
                    ?.copy(
                        id = snapshot.id,
                        unreadCount = snapshot.data?.extractUnreadMap() ?: emptyMap()
                    )
            }
            .catch { emit(null) }
            .flowOn(Dispatchers.IO)

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        conversationsCollection
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Message::class.java) }
            .catch { emit(emptyList()) }
            .flowOn(Dispatchers.IO)

    override fun observeUserConversations(userId: String): Flow<List<Conversation>> =
        conversationsCollection
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)
                        ?.copy(
                            id = doc.id,
                            unreadCount = doc.data?.extractUnreadMap() ?: emptyMap()
                        )
                }
            }
            .flowOn(Dispatchers.IO)

    override fun observeUnreadCount(userId: String): Flow<Int> =
        conversationsCollection
            .whereArrayContains("participants", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.sumOf { doc ->
                    doc.getLong("unread_$userId")?.toInt() ?: 0
                }
            }
            .flowOn(Dispatchers.IO)
}

// ─────────────────────────────────────────────
// Extensions
// ─────────────────────────────────────────────

private fun Map<String, Any>.extractUnreadMap(): Map<String, Int> =
    filterKeys { it.startsWith("unread_") }
        .mapKeys { it.key.removePrefix("unread_") }
        .mapValues { (_, value) -> (value as? Number)?.toInt() ?: 0 }