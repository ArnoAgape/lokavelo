package com.arnoagape.lokavelo.domain.model

import com.arnoagape.lokavelo.data.dto.BikeDto

data class Bike(
    val id: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val category: BikeCategory? = null,
    val brand: String = "",
    val state: BikeState? = null,
    val accessories: List<BikeEquipment> = emptyList(),
    val price: Long = 0L,
    val deposit: Long = 0L,
    val photoUrl: String = "",
    val location: String = "",
    val ownerId: String = "",
    val isElectric: Boolean = false,
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
            state = state,
            accessories = accessories,
            price = price,
            deposit = deposit,
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
                state = dto.state,
                accessories = dto.accessories,
                price = dto.price,
                deposit = dto.deposit,
                photoUrl = dto.photoUrl,
                location = dto.location,
                ownerId = dto.ownerId,
                isElectric = dto.isElectric,
                isAvailable = dto.isAvailable
            )
        }
    }
}