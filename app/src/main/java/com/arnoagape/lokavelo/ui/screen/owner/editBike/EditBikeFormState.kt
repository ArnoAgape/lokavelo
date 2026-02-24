package com.arnoagape.lokavelo.ui.screen.owner.editBike

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull

data class EditBikeFormState(
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

    fun toUpdatedBikeOrNull(original: Bike): Bike? {

        val price = priceText.toCentsOrNull() ?: return null

        val halfDay = halfDayPriceText.toCentsOrNull()
            ?: (price / 2)
        val week = weekPriceText.toCentsOrNull()
            ?: (price * 7 * 70 / 100)

        val month = monthPriceText.toCentsOrNull()
            ?: (price * 30 * 50 / 100)

        val deposit = depositText
            .replace(",", ".")
            .toDoubleOrNull()
            ?.times(100)
            ?.toLong()

        if (price <= 0) return null

        return original.copy(
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

    companion object {
        fun fromBike(bike: Bike): EditBikeFormState {
            return EditBikeFormState(
                title = bike.title,
                description = bike.description,
                location = bike.location,
                priceText = (bike.priceInCents / 100).toString(),
                depositText = bike.depositInCents?.div(100)?.toString() ?: "",
                electric = bike.electric,
                category = bike.category,
                brand = bike.brand,
                condition = bike.condition,
                accessories = bike.accessories
            )
        }
    }

}