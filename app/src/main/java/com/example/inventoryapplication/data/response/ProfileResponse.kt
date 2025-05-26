package com.example.inventoryapplication.data.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val user: UserData?
)

data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("photo_profile")
    val photoProfile: String? = null,
    val role: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)
