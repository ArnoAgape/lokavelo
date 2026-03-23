package com.arnoagape.lokavelo.domain.model

import androidx.annotation.StringRes
import com.arnoagape.lokavelo.R

enum class RentalStatus {
    PENDING,
    COUNTER_OFFER,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    ACTIVE,
    COMPLETED
}

@StringRes
fun RentalStatus.labelRes(): Int =
    when (this) {
        RentalStatus.PENDING -> R.string.rental_status_pending
        RentalStatus.COUNTER_OFFER -> R.string.rental_status_counter_offer
        RentalStatus.ACCEPTED -> R.string.rental_status_reserved
        RentalStatus.DECLINED -> R.string.rental_status_declined
        RentalStatus.CANCELLED -> R.string.rental_status_cancelled
        RentalStatus.ACTIVE -> R.string.rental_status_active
        RentalStatus.COMPLETED -> R.string.rental_status_completed
    }

fun RentalStatus.priority(): Int =
    when (this) {
        RentalStatus.PENDING -> 0
        RentalStatus.COUNTER_OFFER -> 1
        RentalStatus.ACCEPTED,
        RentalStatus.ACTIVE -> 2
        RentalStatus.COMPLETED -> 3
        RentalStatus.DECLINED,
        RentalStatus.CANCELLED -> 4
    }