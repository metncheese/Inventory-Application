package com.example.inventoryapplication.view.signin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.inventoryapplication.databinding.ActivitySignInBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.view.main.MainActivity
import com.example.inventoryapplication.view.signup.SignUpActivity
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private val viewModel by viewModels<SignInViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupActions()
        observeSignIn()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupActions() {
        binding.apply {
            btnSignin.isEnabled = false

            emailEditText.addTextChangedListener(textWatcher)
            passwordEditText.addTextChangedListener(textWatcher)

            btnSignin.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val pass = passwordEditText.text.toString().trim()
                viewModel.postSignIn(email, pass)
            }

            tvSignup.setOnClickListener {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }

            btnBack.setOnClickListener { finish() }
        }
    }

    private fun observeSignIn() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.signInResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    lifecycleScope.launch {
                        // ❗ TUNGGU SAMPAI SESSION BENER-BENER MASUK KE DATASTORE
                        viewModel.getUserSession().collect { session ->
                            if (session != null && session.token.isNotEmpty()) {
                                Log.d("SignInActivity", "✅ Session valid, pindah ke MainActivity")
                                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                finish()
                                return@collect
                            } else {
                                Log.w("SignInActivity", "⏳ Menunggu session masuk...")
                            }
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this, "Login gagal: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private val textWatcher = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateInput()
        }

        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    private fun validateInput() {
        val email = binding.emailEditText.text.toString()
        val pass = binding.passwordEditText.text.toString()
        binding.btnSignin.isEnabled = email.isNotBlank() && pass.isNotBlank()
    }
}
