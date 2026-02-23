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
    val isElectric: Boolean = false,
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val accessories: List<BikeEquipment> = emptyList()
) {
    fun isValid(totalPhotos: Int): Boolean {

        val price = priceText
            .replace(",", ".")
            .toDoubleOrNull()

        return title.isNotBlank() &&
                location.street.isNotBlank() &&
                location.postalCode.isNotBlank() &&
                location.city.isNotBlank() &&
                price != null &&
                price > 0 &&
                totalPhotos in 1..3
    }

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
            electric = isElectric,
            category = category,
            brand = brand,
            condition = condition,
            accessories = accessories
        )
    }

}