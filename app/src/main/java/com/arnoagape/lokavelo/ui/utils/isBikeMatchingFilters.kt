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
    if (filters.bikeCategory != null) {
        if (bike.category != filters.bikeCategory) return false
    }

    // ⚡ Électrique
    if (filters.electricOnly != null) {
        if (bike.electric != filters.electricOnly) return false
    }

    return true
}