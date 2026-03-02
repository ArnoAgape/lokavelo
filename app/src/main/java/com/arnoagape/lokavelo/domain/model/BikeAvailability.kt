package com.arnoagape.lokavelo.domain.model

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R

enum class BikeAvailability {
    AVAILABLE, UNAVAILABLE, RENTING
}

@StringRes
fun BikeAvailability.labelRes(): Int =
    when (this) {
        BikeAvailability.AVAILABLE -> R.string.bike_category_road
        BikeAvailability.RENTING -> R.string.bike_category_mtb
        BikeAvailability.UNAVAILABLE -> R.string.bike_category_city
    }