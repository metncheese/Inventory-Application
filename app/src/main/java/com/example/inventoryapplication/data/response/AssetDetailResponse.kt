package com.example.inventoryapplication.data.response

data class AssetDetailResponse(
    val message: String,
    val data: AssetData
)

data class AssetData(
    val id: Int,
    val name: String,
    val category: String,
    val placement: String,
    val asset_entry: String,
    val expired_date: String,
    val is_can_loan: Boolean,
    val inv_status: String,
    val quantity: String,
    val img_url: String?,
    val created_at: String,
    val updated_at: String
)
