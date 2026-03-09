package com.arnoagape.lokavelo.ui.preview

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message

object PreviewData {

    val bike = Bike(
        id = "bike1",
        title = "Origine Trail Explore",
        ownerId = "owner1",
        priceInCents = 2500,
        photoUrls = emptyList()
    )

    val conversation = Conversation(
        id = "conv1",
        bikeId = "bike1",
        ownerId = "owner1",
        renterId = "user1",
        lastMessage = "Le vélo est dispo 👍",
        lastMessageTime = System.currentTimeMillis()
    )

    val messages = listOf(
        Message(
            id = "1",
            senderId = "user1",
            text = "Salut !",
            createdAt = 1
        ),
        Message(
            id = "2",
            senderId = "user1",
            text = "Le vélo est dispo 👍",
            createdAt = 2
        ),
        Message(
            id = "3",
            senderId = "user2",
            text = "Super ! Je peux venir demain ?",
            createdAt = 3
        )
    )
}