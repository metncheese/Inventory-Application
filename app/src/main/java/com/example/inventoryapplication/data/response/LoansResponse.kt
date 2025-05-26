package com.example.inventoryapplication.data.response

import com.example.inventoryapplication.models.Loan

data class LoansResponse(
    val message: String,
    val data: List<Loan>
)
