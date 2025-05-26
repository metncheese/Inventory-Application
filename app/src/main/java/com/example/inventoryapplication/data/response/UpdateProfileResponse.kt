package com.example.inventoryapplication.data.response

import com.google.gson.annotations.SerializedName

data class UpdateProfileResponse(
    val user: UserDataUpdateProfile?
)

data class UserDataUpdateProfile(
    val id: Int,
    val username: String,
    val email: String,
    val role: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)
