package com.example.inventoryapplication.data.api

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,

    @SerializedName("photo_profile")
    val photoProfile: String?, // ganti jadi String

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

