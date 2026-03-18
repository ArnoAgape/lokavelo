package com.arnoagape.lokavelo.ui.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
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
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

    return formatter.format(this / 100.0)
}

fun Long.toPriceString(): String {
    val formatter = NumberFormat
        .getCurrencyInstance(Locale.getDefault())
        .apply {
            currency = Currency.getInstance("EUR")
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

    return formatter.format(this / 100.0)
}

fun Long?.toPriceText(): String {
    if (this == null) return ""

    val value = this / 100.0

    val symbols = DecimalFormatSymbols(Locale.FRANCE)
    val formatter = DecimalFormat("#.##", symbols)

    return formatter.format(value)
}