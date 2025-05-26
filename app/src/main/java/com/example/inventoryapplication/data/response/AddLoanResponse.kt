package com.example.inventoryapplication.data.response

data class AddLoanResponse(
    val message: String,
    val data: LoanData
)

data class LoanData(
    val inventory_id: Int,
    val user_id: Int,
    val start_at: String,
    val expired_at: String,
    val quantity: Int,
    val updated_at: String,
    val created_at: String,
    val id: Int
)
