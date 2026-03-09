package com.arnoagape.lokavelo.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toHourMinute(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDayLabel(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}