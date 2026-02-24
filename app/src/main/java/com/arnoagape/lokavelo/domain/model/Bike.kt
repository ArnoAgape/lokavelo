package com.arnoagape.lokavelo.domain.model

import com.arnoagape.lokavelo.data.dto.BikeDto
import com.google.firebase.Timestamp
import java.time.Instant

data class Bike(
    val id: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val ownerId: String = "",
    val photoUrls: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val electric: Boolean = false,
    val accessories: List<BikeEquipment> = emptyList(),
    val priceInCents: Long = 0L,
    val priceHalfDayInCents: Long? = null,
    val priceWeekInCents: Long? = null,
    val priceMonthInCents: Long? = null,
    val depositInCents: Long? = null,
    val location: BikeLocation = BikeLocation(),
    val available: Boolean = true,
    val rentalStart: Instant? = null,
    val rentalEnd: Instant? = null
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
            accessories = accessories,
            priceInCents = priceInCents,
            priceHalfDayInCents = priceHalfDayInCents,
            priceWeekInCents = priceWeekInCents,
            priceMonthInCents = priceMonthInCents,
            depositInCents = depositInCents,
            photoUrls = photoUrls,
            location = location,
            ownerId = ownerId,
            electric = electric,
            available = available,
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
                accessories = dto.accessories,
                priceInCents = dto.priceInCents,
                priceHalfDayInCents = dto.priceHalfDayInCents,
                priceWeekInCents = dto.priceWeekInCents,
                priceMonthInCents = dto.priceMonthInCents,
                depositInCents = dto.depositInCents,
                photoUrls = dto.photoUrls,
                location = dto.location,
                ownerId = dto.ownerId,
                electric = dto.electric,
                available = dto.available,
                rentalStart = dto.rentalStart,
                rentalEnd = dto.rentalEnd
            )
        }
    }
}