package com.arnoagape.lokavelo.ui.utils

fun String.clean(): String =
    trim().replace(Regex("\\s+"), " ")