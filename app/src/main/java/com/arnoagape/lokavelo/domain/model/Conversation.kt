package com.arnoagape.lokavelo.domain.model

data class Conversation(
    val id: String = "",
    val bikeId: String = "",
    val ownerId: String = "",
    val renterId: String = "",
    val startDateMillis: Long = 0,
    val endDateMillis: Long = 0,
    val lastMessage: String = "",
    val lastMessageAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)