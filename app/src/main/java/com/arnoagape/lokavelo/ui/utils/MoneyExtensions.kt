package com.arnoagape.lokavelo.ui.utils

import java.text.NumberFormat
import java.util.Locale

private const val CENTS_IN_EURO = 100

fun String.toCentsOrNull(): Long? {
    val normalized = replace(",", ".")
    val double = normalized.toDoubleOrNull() ?: return null
    return (double * CENTS_IN_EURO).toLong()
}

fun Long.toEuroString(): String {
    val formatter = NumberFormat
        .getCurrencyInstance(Locale.FRANCE)
        .apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }

    return formatter.format(this / 100.0)
}

fun Long.toPriceString(): String {
    val formatter = NumberFormat
        .getNumberInstance(Locale.FRANCE)
        .apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }

    return formatter.format(this / 100.0)
}