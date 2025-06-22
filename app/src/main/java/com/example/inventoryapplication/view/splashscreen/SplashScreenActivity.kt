package com.example.inventoryapplication.view.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inventoryapplication.R
import com.example.inventoryapplication.view.welcome.WelcomeActivity
import com.example.inventoryapplication.view.main.MainActivity
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        userPreference = UserPreference.getInstance(applicationContext.dataStore)

        // Pakai coroutine untuk cek session di background
        CoroutineScope(Dispatchers.Main).launch {
            val token = userPreference.getSession().first().token // pastikan pakai .first() dari kotlinx.coroutines.flow
            if (token.isNotEmpty()) {
                // Masih login, langsung ke MainActivity (Home)
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Belum login, lanjut WelcomeActivity
                val intent = Intent(this@SplashScreenActivity, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
