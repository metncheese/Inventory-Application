package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.models.Asset

data class LoanDetailResponse(
    val message: String,
    val data: LoanDetailData
)

data class LoanDetailData(
    val id: Int,
    val inventory_id: String,
    val user_id: String,
    val start_at: String,
    val expired_at: String,
    val quantity: String,
    val created_at: String,
    val updated_at: String,
    val inventory: Asset,
    val user: User
)

