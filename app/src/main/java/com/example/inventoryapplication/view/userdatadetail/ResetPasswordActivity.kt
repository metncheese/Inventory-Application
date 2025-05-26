package com.example.inventoryapplication.view.userdatadetail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.inventoryapplication.databinding.ActivityResetPasswordBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: ResetPasswordViewModel
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar back button
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Ambil userId dari intent (FIX: pakai key yang benar)
        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "ID pengguna tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this)).get(ResetPasswordViewModel::class.java)

        binding.btnChangePassword.setOnClickListener {
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, "Password dan konfirmasi harus sama", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.resetPassword(userId, newPassword)
            }
        }

        viewModel.resetPasswordResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = android.view.View.VISIBLE
                is Result.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman sebelumnya
                }
                is Result.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Gagal reset password: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
