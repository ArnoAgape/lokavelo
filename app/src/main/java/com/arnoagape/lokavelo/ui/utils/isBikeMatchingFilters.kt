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

        android.location.Location.distanceBetween(
            filters.center.latitude,
            filters.center.longitude,
            bike.location.latitude,
            bike.location.longitude,
            results
        )

        val distanceKm = results[0] / 1000.0

        if (distanceKm > filters.maxDistanceKm) return false
    }

    return true
}