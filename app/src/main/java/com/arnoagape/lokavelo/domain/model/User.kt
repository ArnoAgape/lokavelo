package com.arnoagape.lokavelo.domain.model

import com.arnoagape.lokavelo.data.dto.UserDto

data class User(
    val id: String = "",
    val displayName: String? = "",
    val photoUrl: String? = "",
    val phoneNumber: String? = "",
    val email: String? = "",
    val address: String? = "",
    val bio: String? = ""
) {
    fun toDto(): UserDto {
        return UserDto(
            id = id,
            displayName = displayName,
            photoUrl = photoUrl,
            phoneNumber = phoneNumber,
            email = email,
            address = address,
            bio = bio
        )
    }

    companion object {
        fun fromDto(dto: UserDto): User {
            return User(
                id = dto.id,
                displayName = dto.displayName,
                photoUrl = dto.photoUrl,
                phoneNumber = dto.phoneNumber,
                email = dto.email,
                address = dto.address,
                bio = dto.bio
            )
        }
    }
}