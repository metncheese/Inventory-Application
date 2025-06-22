package com.example.inventoryapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.inventoryapplication.R
import com.example.inventoryapplication.data.api.ApiConfig
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class TenggatNotificationWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                // Tidak login / token invalid
                return@withContext Result.success()
            }
            val bearerToken = "Bearer $token"
            val apiService = ApiConfig.getApiService()
            val today = LocalDate.now()
            val sentMessages = mutableSetOf<String>() // Prevent duplicate notification

            // Cek Assets
            val assetResponse = apiService.getAdminAssets(bearerToken)
            if (assetResponse.isSuccessful) {
                assetResponse.body()?.data?.forEach { asset ->
                    asset.expired_date?.substring(0,10)?.let {
                        Log.d("TenggatNotif", "Tanggal expired: $it")
                        val expiredDate = LocalDate.parse(it)
                        val daysDiff = ChronoUnit.DAYS.between(today, expiredDate)
                        Log.d("TenggatNotif", "Asset: ${asset.name}, today: $today, expired: $expiredDate, daysDiff: $daysDiff")
                        when (daysDiff) {
                            3L -> {
                                Log.d("TenggatNotif", "Notifikasi 3 hari sebelum expired")
                                val msg = "Masa waktu peminjaman ${asset.name} yang di pinjam oleh perusahaan akan habis dalam 3 hari lagi dari waktu peminjamannya, silahkan untuk dipersiapkan kelanjutannya."
                                if (sentMessages.add(msg)) sendNotification("Pengingat Aset", msg)
                            }
                            0L -> {
                                Log.d("TenggatNotif", "Notifikasi expired hari ini")
                                val msg = "Aset ${asset.name} yang di pinjam oleh perusahaan, masa waktu pinjamanya telah berakhir, jangan lupa untuk di kembalikan yaaa. Terimakasih ☺️"
                                if (sentMessages.add(msg)) sendNotification("Tenggat Aset", msg)
                            }
                        }
                    }
                }
            }

            // Cek Loans
            val loanResponse = apiService.getAdminLoans(bearerToken)
            if (loanResponse.isSuccessful) {
                loanResponse.body()?.data?.forEach { loan ->
                    loan.expiredAt?.substring(0,10)?.let {
                        Log.d("TenggatNotif", "Tanggal expired: $it")
                        val expiredDate = LocalDate.parse(it)
                        val daysDiff = ChronoUnit.DAYS.between(today, expiredDate)
                        val assetName = loan.inventory.name
                        val borrower = loan.user.username
                        when (daysDiff) {
                            3L -> {
                                Log.d("TenggatNotif", "Notifikasi 3 hari sebelum expired")
                                val msg = "Masa waktu peminjaman $assetName yang di pinjam $borrower akan habis dalam 3 hari lagi, jangan lupa untuk di cek dan diingatkan kepada peminjam 😊"
                                if (sentMessages.add(msg)) sendNotification("Pengingat Peminjaman Karyawan", msg)
                            }
                            0L -> {
                                Log.d("TenggatNotif", "Notifikasi expired hari ini")
                                val msg = "$assetName yang dipinjam oleh $borrower, masa waktu pinjamnya telah berakhir. Pastikan sudah di kembalikan yaaa, Terimakasih ☺️"
                                if (sentMessages.add(msg)) sendNotification("Tenggat Peminjaman Karyawan", msg)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        Log.d("TenggatNotif", "Eksekusi manager.notify dengan $title | $message")
        val channelId = "tenggat_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Tenggat Notifikasi", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(Random().nextInt(), builder.build())
    }

    private suspend fun getToken(): String? {
        val userPref = UserPreference.getInstance(applicationContext.dataStore)
        val session = userPref.getSession().first()
        return session.token.takeIf { it.isNotEmpty() }
    }
}
