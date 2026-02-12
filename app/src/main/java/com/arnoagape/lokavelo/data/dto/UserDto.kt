package com.arnoagape.lokavelo.data.dto

import java.io.Serializable

data class UserDto(
    val id: String = "",
    val displayName: String? = "",
    val photoUrl: String? = "",
    val phoneNumber: String? = "",
    val email: String? = "",
    val address: String? = "",
    val bio: String? = ""
) : Serializable