package com.example.inventoryapplication.view.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.inventoryapplication.databinding.ActivitySignUpBinding
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.view.signin.SignInActivity
import com.example.inventoryapplication.data.api.ApiConfig
import com.example.inventoryapplication.models.ViewModelFactory


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val signUpViewModel: SignUpViewModel by viewModels {
        ViewModelFactory(
            UserRepository.getInstance(
                UserPreference.getInstance(dataStore),
                ApiConfig.getApiService()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setupAction()
        setupObserver()
    }

    private fun setupAction() {
        binding.apply {
            btnSignup.setOnClickListener {
                val username = userNameEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    signUpViewModel.postSignUp(username, email, password)
                } else {
                    Toast.makeText(this@SignUpActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }

            tvSignin.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finish()
            }

            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun setupObserver() {
        signUpViewModel.signUpResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Result.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(this, "Sign up success!", Toast.LENGTH_SHORT).show()
                    Log.d("SignUpActivity", "Sign up success: ${result.data}")
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(this, "Sign up failed: ${result.exception.message}", Toast.LENGTH_SHORT).show() // Perbaiki di sini
                    Log.e("SignUpActivity", "Sign up failed: ${result.exception.message}")
                }

            }
        }
    }
}
