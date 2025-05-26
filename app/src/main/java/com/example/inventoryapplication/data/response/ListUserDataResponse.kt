package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.data.api.User

data class ListUserDataResponse(
    val message: String,
    val data: List<User>
)