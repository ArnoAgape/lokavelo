package com.arnoagape.lokavelo.ui.utils

import android.content.Context
import com.arnoagape.lokavelo.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private const val CENTS_IN_EURO = 100

// ─────────────────────────────────────────────
// Money
// ─────────────────────────────────────────────

fun String.toCentsOrNull(): Long? {
    val normalized = replace(",", ".")
    val double = normalized.toDoubleOrNull() ?: return null
    return (double * CENTS_IN_EURO).toLong()
}

fun Long.toEuroString(): String =
    NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(this / 100.0)

fun Long.toPriceString(): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance("EUR")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(this / 100.0)

fun Long?.toPriceText(): String {
    if (this == null) return ""
    return DecimalFormat("#.##", DecimalFormatSymbols(Locale.FRANCE))
        .format(this / 100.0)
}

// ─────────────────────────────────────────────
// Date / Duration
// ─────────────────────────────────────────────

fun Int.toDaysString(context: Context): String =
    context.resources.getQuantityString(R.plurals.days, this, this)