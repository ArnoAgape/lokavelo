package com.arnoagape.lokavelo.ui.utils

import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import java.time.LocalDate
import java.time.ZoneId

fun List<Rental>.mainRental(): Rental? =
    firstOrNull { it.status == RentalStatus.ACTIVE }
        ?: filter {
            it.status == RentalStatus.ACCEPTED || it.status == RentalStatus.PENDING
        }.minByOrNull { it.startDate }

fun Rental.toDisplayStatus(today: LocalDate): RentalStatus {
    val startLocal = startDate.atZone(ZoneId.systemDefault()).toLocalDate()
    return when (status) {
        RentalStatus.ACTIVE -> RentalStatus.ACTIVE
        RentalStatus.ACCEPTED if startLocal > today -> RentalStatus.ACCEPTED
        RentalStatus.COMPLETED -> RentalStatus.COMPLETED
        else -> status
    }
}