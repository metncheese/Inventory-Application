package com.example.inventoryapplication.models

import com.example.inventoryapplication.data.api.User
import com.google.gson.annotations.SerializedName

data class Loan(
    val id: Int,

    @SerializedName("inventory_id")
    val inventoryId: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("start_at")
    val startAt: String,

    @SerializedName("expired_at")
    val expiredAt: String,

    val quantity: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    val inventory: Asset, // nested object dari JSON (nama field tetap "inventory" di JSON)
    val user: User         // nested object dari JSON
)
