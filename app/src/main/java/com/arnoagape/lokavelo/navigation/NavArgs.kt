package com.arnoagape.lokavelo.navigation

import androidx.navigation.NavBackStackEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun NavBackStackEntry.stringArg(name: String): String? {
    return arguments?.getString(name)
}

fun NavBackStackEntry.localDateArg(name: String): LocalDate? {

    val millis = arguments?.getLong(name) ?: return null

    if (millis <= 0) return null

    return Instant
        .ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}