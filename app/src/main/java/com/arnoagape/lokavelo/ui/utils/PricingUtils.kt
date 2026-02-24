package com.arnoagape.lokavelo.ui.utils

fun calculateDiscountPercent(
    basePerDayCents: Long,
    discountedCents: Long,
    days: Int
): Int {

    val normal = basePerDayCents * days
    if (normal == 0L) return 0

    val ratio = discountedCents.toDouble() / normal.toDouble()
    return (100 - (ratio * 100))
        .toInt()
        .coerceIn(0, 100)
}