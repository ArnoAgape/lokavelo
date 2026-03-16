package com.arnoagape.lokavelo.ui.preview

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.User
import java.time.Instant

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

    val user = User(
        id = "user1",
        displayName = "John Doe",
        photoUrl = null,
        phoneNumber = "0606060606",
        email = "johndoe@mail.fr",
        address = "1 avenue de la République",
        bio = "J'aime manger bio"
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

    val rental = Rental(
        id = "rental1",
        bikeId = "bike1",
        ownerId = "owner1",
        renterId = "user1",
        startDate = Instant.parse("2026-06-12T10:00:00Z"),
        endDate = Instant.parse("2026-06-15T10:00:00Z"),
        priceTotalInCents = 7550,
        basePriceInCents = 5000,
        serviceFeeInCents = 10,
        depositInCents = 20000,
        status = RentalStatus.PENDING,
        createdAt = Instant.now()
    )
}