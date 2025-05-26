package com.example.inventoryapplication.models

data class UserModel(
    val id: Int? = null,
    val token: String,
    val isSignIn: Boolean,
    val username: String? = null,
    val email: String? = null,
    val photoProfile: String? = null,
    val role: String? = null
)
