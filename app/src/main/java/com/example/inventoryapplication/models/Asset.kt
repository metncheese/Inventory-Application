package com.example.inventoryapplication.models

import com.google.gson.annotations.SerializedName

data class Asset(
    val id: Int,
    val name: String,
    val category: String,
    val placement: String,
    val asset_entry: String,
    val expired_date: String,
    val is_can_loan: Boolean,
    val inv_status: String,
    @SerializedName("quantity")
    val quantity: String,

    @SerializedName("img_url")
    val photo_asset: String?, // ganti jadi String

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

