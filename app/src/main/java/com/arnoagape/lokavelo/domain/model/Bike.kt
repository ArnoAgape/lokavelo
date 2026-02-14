package com.arnoagape.lokavelo.domain.model

import com.arnoagape.lokavelo.data.dto.BikeDto
import com.google.firebase.Timestamp

data class Bike(
    val id: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val ownerId: String = "",

    val photoUrl: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val isElectric: Boolean = false,
    val accessories: List<BikeEquipment> = emptyList(),

    val priceInCents: Long? = null,
    val depositInCents: Long? = null,

    val location: BikeLocation,

    val isAvailable: Boolean = true
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
            depositInCents = depositInCents,
            photoUrl = photoUrl,
            location = location,
            ownerId = ownerId,
            isElectric = isElectric,
            isAvailable = isAvailable
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
                depositInCents = dto.depositInCents,
                photoUrl = dto.photoUrl,
                location = dto.location,
                ownerId = dto.ownerId,
                isElectric = dto.isElectric,
                isAvailable = dto.isAvailable
            )
        }
    }
}