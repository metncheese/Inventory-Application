package com.example.inventoryapplication.view.userdatadetail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.ActivityUserDataDetailBinding
import com.example.inventoryapplication.models.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserDataDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDataDetailBinding
    private lateinit var viewModel: UserDataDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDataDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this))[UserDataDetailViewModel::class.java]

        lifecycleScope.launch {
            val repo = ViewModelFactory.getInstance(applicationContext).let {
                val field = it.javaClass.getDeclaredField("repository")
                field.isAccessible = true
                field.get(it) as com.example.inventoryapplication.utils.UserRepository
            }
            val session = repo.getSession().first()
            viewModel.fetchUserDetail(session.token, userId)
        }

        viewModel.user.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, "User tidak ditemukan atau bukan user biasa", Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }

            binding.fillUsername.text = user.username
            binding.fillEmail.text = user.email

            val photoUrl = if (!user.photoProfile.isNullOrBlank()) {
                if (user.photoProfile.startsWith("http")) user.photoProfile
                else "https://fauziewan.my.id/storage/${user.photoProfile.trimStart('/')}"
            } else null

            Glide.with(this)
                .load(photoUrl ?: R.drawable.ic_profile_placeholder)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(binding.itemImage)
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("user_id", userId) // Ubah ke "user_id" biar konsisten
            startActivity(intent)
        }
    }
}
