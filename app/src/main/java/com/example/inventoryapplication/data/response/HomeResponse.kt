package com.example.inventoryapplication.data.response

import com.google.gson.annotations.SerializedName

data class HomeResponse(

    val message: String,
    val data: DashboardData
)

data class DashboardData(
    @SerializedName("total_aset") val totalAset: Int,
    @SerializedName("aset_tersedia") val asetTersedia: Int,
    @SerializedName("aset_dipinjam") val asetDipinjam: Int,
    @SerializedName("total_peminjaman") val totalPeminjaman: Int
)

