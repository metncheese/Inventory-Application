package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.models.Asset

data class AssetsResponse(
    val message: String,
    val data: List<Asset>
)
