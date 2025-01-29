package com.example.inventoryapplication.view.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inventoryapplication.databinding.ActivitySignUpBinding
import com.example.inventoryapplication.view.signin.SignInActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.tvSignin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}