package com.arnoagape.lokavelo.ui.screen.owner.editBike

import android.net.Uri
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed interface EditBikeEvent {

    data class TitleChanged(val title: String) : EditBikeEvent
    data class DescriptionChanged(val description: String) : EditBikeEvent
    data class AddressChanged(val address: String) : EditBikeEvent
    data class Address2Changed(val address2: String) : EditBikeEvent
    data class ZipChanged(val zipCode: String) : EditBikeEvent
    data class CityChanged(val city: String) : EditBikeEvent
    data class PriceChanged(val priceText: String) : EditBikeEvent
    data class DepositChanged(val depositText: String) : EditBikeEvent
    data class ElectricChanged(val isElectric: Boolean) : EditBikeEvent
    data class CategoryChanged(val category: BikeCategory) : EditBikeEvent
    data class BrandChanged(val brand: String) : EditBikeEvent
    data class StateChanged(val state: BikeCondition) : EditBikeEvent
    data class AccessoriesChanged(val accessories: List<BikeEquipment>) : EditBikeEvent
    data class AddPhoto(val uri: Uri) : EditBikeEvent
    data class RemovePhoto(val uri: Uri) : EditBikeEvent
    data class ReplacePhoto(val oldUri: Uri, val newUri: Uri) : EditBikeEvent
    data object Submit : EditBikeEvent
}
