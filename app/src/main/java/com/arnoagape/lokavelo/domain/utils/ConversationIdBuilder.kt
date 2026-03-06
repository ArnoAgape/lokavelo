package com.arnoagape.lokavelo.domain.utils

object ConversationIdBuilder {

    fun build(
        bikeId: String,
        ownerId: String,
        renterId: String,
        start: Long,
        end: Long
    ): String {
        return "${bikeId}_${ownerId}_${renterId}_${start}_${end}"
    }
}