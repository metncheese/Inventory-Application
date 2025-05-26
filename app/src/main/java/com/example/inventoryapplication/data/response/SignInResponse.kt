package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.data.api.User
import com.google.gson.annotations.SerializedName

data class SignInResponse(
    val message: String,

    @SerializedName(value = "access_token")
    val token: String, // Diubah dari access_token -> token lokal

    @SerializedName(value = "token_type")
    val tokenType: String,

    val role: String,
    val user: User,
)
