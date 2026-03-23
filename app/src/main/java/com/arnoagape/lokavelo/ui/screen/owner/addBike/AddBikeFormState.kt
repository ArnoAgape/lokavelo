package com.arnoagape.lokavelo.ui.screen.owner.addBike

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.arnoagape.lokavelo.ui.utils.clean
import com.arnoagape.lokavelo.ui.utils.toCentsOrNull

data class AddBikeFormState(
    val title: String = "",
    val description: String = "",
    val location: BikeLocation = BikeLocation(),
    val priceText: String = "",
    val twoDaysPriceText: String = "",
    val weekPriceText: String = "",
    val monthPriceText: String = "",
    val depositText: String = "",
    val electric: Boolean = false,
    val size: BikeSize? = null,
    val category: BikeCategory? = null,
    val brand: String = "",
    val condition: BikeCondition? = null,
    val accessories: List<BikeEquipment> = emptyList(),
    val available: Boolean = true,
    val minDaysRentalText: String = "",

    val photosError: Boolean = false,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val categoryError: Boolean = false,
    val sizeError: Boolean = false,
    val conditionError: Boolean = false,
    val priceError: Boolean = false,
    val streetError: Boolean = false,
    val postalCodeError: Boolean = false,
    val cityError: Boolean = false,
    val minDaysRentalError: Boolean = false,

    val isTwoDaysManuallyEdited: Boolean = false,
    val isWeekManuallyEdited: Boolean = false,
    val isMonthManuallyEdited: Boolean = false
) {

    fun toBikeOrNull(): Bike? {

        val cleanTitle = title.clean()
        val cleanDescription = description.clean()
        val cleanBrand = brand.clean()

        if (cleanTitle.isBlank() || cleanDescription.isBlank()) return null

        val price = priceText.toCentsOrNull() ?: return null

        val twoDays = twoDaysPriceText.toCentsOrNull()
            ?: (price * 2 * 90 / 100)
        val week = weekPriceText.toCentsOrNull()
            ?: (price * 7 * 70 / 100)

        val month = monthPriceText.toCentsOrNull()
            ?: (price * 30 * 50 / 100)

        val deposit = depositText.toCentsOrNull()

        val minDaysRental = minDaysRentalText.toIntOrNull() ?: return null

        if (price <= 0) return null

        return Bike(
            title = cleanTitle,
            description = cleanDescription,
            location = location,
            priceInCents = price,
            priceTwoDaysInCents = twoDays,
            priceWeekInCents = week,
            priceMonthInCents = month,
            depositInCents = deposit,
            electric = electric,
            size = size,
            category = category,
            brand = cleanBrand,
            condition = condition,
            accessories = accessories,
            available = available,
            minDaysRental = minDaysRental
        )
    }

}