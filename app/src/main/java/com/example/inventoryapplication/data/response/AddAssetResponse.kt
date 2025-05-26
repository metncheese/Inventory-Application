package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.models.Asset

data class AddAssetResponse(
    val message: String,
    val data: Asset
)
