package com.arnoagape.lokavelo.ui.utils

import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters

fun isBikeMatchingFilters(
    bike: Bike,
    filters: SearchFilters
): Boolean {

    // 📍 Filtre distance uniquement si activé
    if (filters.center != null && filters.maxDistanceKm != null) {

        val results = FloatArray(1)

        if (bike.location.latitude != null && bike.location.longitude != null) {
            android.location.Location.distanceBetween(
                filters.center.latitude,
                filters.center.longitude,
                bike.location.latitude,
                bike.location.longitude,
                results
            )
        }

        val distanceKm = results[0] / 1000.0

        if (distanceKm > filters.maxDistanceKm) return false
    }

    // 🚲 Catégorie
    if (filters.bikeCategories.isNotEmpty()) {
        if (bike.category !in filters.bikeCategories) return false
    }

    // ⚡ Électrique
    if (filters.bikeMotor.isNotEmpty()) {
        if (bike.motorType !in filters.bikeMotor) return false
    }

    // 📐 Taille
    if (filters.bikeSizes.isNotEmpty()) {
        if (bike.size !in filters.bikeSizes) return false
    }

    // 🎒 Accessoires — le vélo doit posséder TOUS les accessoires sélectionnés
    if (filters.accessories.isNotEmpty()) {
        if (!bike.accessories.containsAll(filters.accessories)) return false
    }

    // 💶 Prix
    if (filters.minPrice > 0f || filters.maxPrice < 100f) {
        val priceInEuros = bike.priceInCents / 100f
        if (priceInEuros < filters.minPrice || priceInEuros > filters.maxPrice) return false
    }

    return true
}