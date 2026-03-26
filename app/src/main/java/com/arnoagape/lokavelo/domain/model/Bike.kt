package com.arnoagape.lokavelo.domain.model

import com.arnoagape.lokavelo.data.dto.BikeDto
import com.google.firebase.Timestamp
import java.time.Instant

data class Bike(
    val id: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val ownerId: String = "",
    val ownerName: String = "",
    val photoUrls: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val motorType: BikeMotor = BikeMotor.REGULAR,
    val size: BikeSize? = null,
    val accessories: List<BikeEquipment> = emptyList(),
    val priceInCents: Long = 0L,
    val priceTwoDaysInCents: Long? = null,
    val priceWeekInCents: Long? = null,
    val priceMonthInCents: Long? = null,
    val depositInCents: Long? = null,
    val location: BikeLocation = BikeLocation(),
    val available: Boolean = true,
    val minDaysRental: Int = 1,
    val rentalStart: Instant? = null,
    val rentalEnd: Instant? = null,
    val viewCount: Int = 0,
    val favoriteCount: Int = 0
) {

    fun toDto(): BikeDto {
        return BikeDto(
            id = id,
            createdAt = createdAt,
            title = title,
            description = description,
            category = category,
            brand = brand,
            condition = condition,
            motorType = motorType,
            size = size,
            accessories = accessories,
            priceInCents = priceInCents,
            priceTwoDaysInCents = priceTwoDaysInCents,
            priceWeekInCents = priceWeekInCents,
            priceMonthInCents = priceMonthInCents,
            depositInCents = depositInCents,
            photoUrls = photoUrls,
            location = location,
            ownerId = ownerId,
            ownerName = ownerName,
            available = available,
            minDaysRental = minDaysRental,
            rentalStart = rentalStart,
            rentalEnd = rentalEnd
        )
    }

    companion object {
        fun fromDto(dto: BikeDto): Bike {
            return Bike(
                id = dto.id,
                createdAt = dto.createdAt,
                title = dto.title,
                description = dto.description,
                category = dto.category,
                brand = dto.brand,
                condition = dto.condition,
                motorType = dto.motorType,
                size = dto.size,
                accessories = dto.accessories,
                priceInCents = dto.priceInCents,
                priceTwoDaysInCents = dto.priceTwoDaysInCents,
                priceWeekInCents = dto.priceWeekInCents,
                priceMonthInCents = dto.priceMonthInCents,
                depositInCents = dto.depositInCents,
                photoUrls = dto.photoUrls,
                location = BikeLocation(
                    street = dto.location?.street ?: "",
                    postalCode = dto.location?.postalCode ?: "",
                    city = dto.location?.city ?: "",
                    latitude = dto.location?.latitude ?: 0.0,
                    longitude = dto.location?.longitude ?: 0.0
                ),
                ownerId = dto.ownerId,
                ownerName = dto.ownerName,
                available = dto.available,
                minDaysRental = dto.minDaysRental,
                rentalStart = dto.rentalStart,
                rentalEnd = dto.rentalEnd
            )
        }
    }
}