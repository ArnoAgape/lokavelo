package com.arnoagape.lokavelo.domain.model

data class BikeLocation(
    val street: String = "",
    val postalCode: String = "",
    val city: String = "",
    val country: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AddressSuggestion(
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val street: String?,
    val city: String?,
    val postalCode: String?,
    val country: String?
)