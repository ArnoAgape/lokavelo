package com.arnoagape.lokavelo.ui.screen.owner.addBike

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull

data class AddBikeFormState(
    val title: String = "",
    val description: String = "",
    val location: BikeLocation = BikeLocation(),
    val priceText: String = "",
    val halfDayPriceText: String = "",
    val weekPriceText: String = "",
    val monthPriceText: String = "",
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
    val cityError: Boolean = false,

    val isHalfDayManuallyEdited: Boolean = false,
    val isWeekManuallyEdited: Boolean = false,
    val isMonthManuallyEdited: Boolean = false
) {

    fun toBikeOrNull(): Bike? {

        val price = priceText.toCentsOrNull() ?: return null
        if (price <= 0) return null

        val halfDay = halfDayPriceText.toCentsOrNull()
            ?: (price / 2)
        val week = weekPriceText.toCentsOrNull()
            ?: (price * 7 * 70 / 100)

        val month = monthPriceText.toCentsOrNull()
            ?: (price * 30 * 50 / 100)

        val deposit = depositText.toCentsOrNull()

        return Bike(
            title = title,
            description = description,
            location = location,
            priceInCents = price,
            priceHalfDayInCents = halfDay,
            priceWeekInCents = week,
            priceMonthInCents = month,
            depositInCents = deposit,
            electric = electric,
            category = category,
            brand = brand,
            condition = condition,
            accessories = accessories
        )
    }

}