package com.arnoagape.lokavelo.domain.model

data class Conversation(
    val id: String = "",
    val bikeId: String = "",
    val ownerId: String = "",
    val renterId: String = "",
    val startDateMillis: Long = 0,
    val endDateMillis: Long = 0,
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val lastSenderId: String = "",
    val createdAt: Long = 0
)

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Long = 0
)