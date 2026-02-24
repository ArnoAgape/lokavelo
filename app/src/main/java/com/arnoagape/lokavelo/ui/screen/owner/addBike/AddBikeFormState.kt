package com.arnoagape.lokavelo.ui.screen.owner.addBike

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation

data class AddBikeFormState(
    val title: String = "",
    val description: String = "",
    val location: BikeLocation = BikeLocation(),
    val priceText: String = "",
    val depositText: String = "",
    val electric: Boolean = false,
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val accessories: List<BikeEquipment> = emptyList(),

    val photosError: Boolean = false,
    val titleError: Boolean = false,
    val categoryError: Boolean = false,
    val conditionError: Boolean = false,
    val priceError: Boolean = false,
    val streetError: Boolean = false,
    val postalCodeError: Boolean = false,
    val cityError: Boolean = false
) {

    fun toBikeOrNull(): Bike? {

        val price = priceText
            .replace(",", ".")
            .toDoubleOrNull()
            ?.times(100)
            ?.toLong()
            ?: return null

        val deposit = depositText
            .replace(",", ".")
            .toDoubleOrNull()
            ?.times(100)
            ?.toLong()

        if (price <= 0) return null

        return Bike(
            title = title,
            description = description,
            location = location,
            priceInCents = price,
            depositInCents = deposit,
            electric = electric,
            category = category,
            brand = brand,
            condition = condition,
            accessories = accessories
        )
    }

}