package com.arnoagape.lokavelo.domain.model

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R

enum class BikeMotor {
    REGULAR,
    ELECTRIC
}

@StringRes
fun BikeMotor.labelRes(): Int =
    when (this) {
        BikeMotor.REGULAR -> R.string.muscular
        BikeMotor.ELECTRIC -> R.string.electric
    }