package com.arnoagape.lokavelo.ui.screen.owner.addBike

import android.net.Uri
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed interface AddBikeEvent {

    data class TitleChanged(val title: String) : AddBikeEvent
    data class DescriptionChanged(val description: String) : AddBikeEvent
    data class AddressChanged(val address: String) : AddBikeEvent
    data class Address2Changed(val address2: String) : AddBikeEvent
    data class ZipChanged(val zipCode: String) : AddBikeEvent
    data class CityChanged(val city: String) : AddBikeEvent
    data class PriceChanged(val value: String) : AddBikeEvent
    data class HalfDayPriceChanged(val value: String) : AddBikeEvent
    data class WeekPriceChanged(val value: String) : AddBikeEvent
    data class MonthPriceChanged(val value: String) : AddBikeEvent
    data class DepositChanged(val depositText: String) : AddBikeEvent
    data class ElectricChanged(val electric: Boolean) : AddBikeEvent
    data class CategoryChanged(val category: BikeCategory) : AddBikeEvent
    data class BrandChanged(val brand: String) : AddBikeEvent
    data class StateChanged(val state: BikeCondition) : AddBikeEvent
    data class AccessoriesChanged(val accessories: List<BikeEquipment>) : AddBikeEvent
    data class AddPhoto(val uri: Uri) : AddBikeEvent
    data class RemovePhoto(val id: String) : AddBikeEvent
    data class ReplacePhoto(val id: String, val newUri: Uri) : AddBikeEvent

    data object Submit : AddBikeEvent
}
