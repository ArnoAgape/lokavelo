package com.arnoagape.lokavelo.data.dto

import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.google.firebase.Timestamp
import java.io.Serializable

data class BikeDto(
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
) : Serializable