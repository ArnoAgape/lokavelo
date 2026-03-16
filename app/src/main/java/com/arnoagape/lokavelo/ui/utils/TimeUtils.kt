package com.arnoagape.lokavelo.ui.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Long.toHourMinute(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDayLabel(): String {
    val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDayLabelYear(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toLocalDateFromEpochDay(): LocalDate {
    return LocalDate.ofEpochDay(this)
}

fun Instant.toDateString(): String {
    val formatter = DateTimeFormatter
        .ofPattern("dd MMM")
        .withLocale(Locale.FRANCE)
        .withZone(ZoneId.systemDefault())

    return formatter.format(this)
}

fun Instant.toLocalDate(): LocalDate =
    atZone(ZoneId.systemDefault()).toLocalDate()