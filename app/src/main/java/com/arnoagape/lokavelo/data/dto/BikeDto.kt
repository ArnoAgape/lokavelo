package com.arnoagape.lokavelo.data.dto

import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.BikeMotor
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.google.firebase.Timestamp
import java.io.Serializable
import java.time.Instant

data class BikeDto(
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
    val location: BikeLocation? = null,
    val available: Boolean = true,
    val minDaysRental: Int = 1,
    val rentalStart: Instant? = null,
    val rentalEnd: Instant? = null
) : Serializable