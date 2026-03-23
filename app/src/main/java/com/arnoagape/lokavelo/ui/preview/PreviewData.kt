package com.arnoagape.lokavelo.ui.preview

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.arnoagape.lokavelo.domain.model.BikeWithRentals
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.domain.model.User
import java.time.Instant
import java.time.temporal.ChronoUnit

object PreviewData {

    val bike = Bike(
        title = "Vélo gravel Origine Trail Explore",
        description = "Vélo en super état avec fourche suspendue",
        category = BikeCategory.GRAVEL,
        brand = "Origine",
        size = BikeSize.M,
        condition = BikeCondition.LIKE_NEW,
        accessories = listOf(
            BikeEquipment.HANDLEBAR_BAG, BikeEquipment.MUDGUARD, BikeEquipment.BELL,
            BikeEquipment.REFLECTIVE_VEST
        ),
        priceInCents = 2500,
        priceTwoDaysInCents = 1250,
        priceWeekInCents = 10000,
        priceMonthInCents = 25000,
        depositInCents = 50000,
        location = BikeLocation(
            street = "4 bd Longchamp",
            postalCode = "13001",
            city = "Marseille"
        ),
        photoUrls = emptyList(),
        rentalStart = Instant.parse("2026-02-21T16:30:00Z"),
        rentalEnd = Instant.parse("2026-02-28T11:30:00Z"),
    )

    val bikes = listOf(
        Bike(
            id = "bike1",
            title = "Origine Trail Explore",
            ownerId = "owner1",
            priceInCents = 2000,
            photoUrls = emptyList()
        ),
        Bike(
            id = "bike2",
            title = "Scott Sub 30",
            ownerId = "owner1",
            priceInCents = 1500,
            photoUrls = emptyList()
        ),
        Bike(
            id = "bike3",
            title = "Riverside Touring 920",
            ownerId = "owner1",
            priceInCents = 2200,
            photoUrls = emptyList()
        ),
    )

    val bikesWithRentals = listOf(

        BikeWithRentals(
            bike = Bike(
                id = "bike1",
                title = "Origine Trail Explore",
                ownerId = "owner1",
                priceInCents = 2000,
                photoUrls = emptyList(),
                brand = "Origine Trail Explore"
            ),
            rentals = listOf(
                Rental(
                    id = "r1",
                    bikeId = "bike1",
                    startDate = Instant.now().minus(1, ChronoUnit.DAYS),
                    endDate = Instant.now().plus(2, ChronoUnit.DAYS),
                    status = RentalStatus.ACTIVE
                )
            )
        ),

        BikeWithRentals(
            bike = Bike(
                id = "bike2",
                title = "Scott Sub 30",
                ownerId = "owner1",
                priceInCents = 1500,
                photoUrls = emptyList(),
                brand = "Origine Trail Explore"
            ),
            rentals = listOf(
                Rental(
                    id = "r2",
                    bikeId = "bike2",
                    startDate = Instant.now().plus(2, ChronoUnit.DAYS),
                    endDate = Instant.now().plus(5, ChronoUnit.DAYS),
                    status = RentalStatus.ACCEPTED
                )
            )
        ),

        BikeWithRentals(
            bike = Bike(
                id = "bike3",
                title = "Riverside Touring 920",
                ownerId = "owner1",
                priceInCents = 2200,
                photoUrls = emptyList(),
                brand = "Origine Trail Explore"
            ),
            rentals = emptyList() // dispo
        )
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