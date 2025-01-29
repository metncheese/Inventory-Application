package com.example.inventoryapplication.view.signin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inventoryapplication.databinding.ActivitySignInBinding
import com.example.inventoryapplication.view.main.MainActivity
import com.example.inventoryapplication.view.signup.SignUpActivity

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnSignin.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}