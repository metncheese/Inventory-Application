package com.example.inventoryapplication.utils

data class AddLoanRequest(
    val inventory_id: Int,
    val user_id: Int,
    val start_at: String, // Format ISO 8601: yyyy-MM-dd'T'HH:mm:ss'Z'
    val expired_at: String,
    val quantity: Int
)
