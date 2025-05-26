package com.example.inventoryapplication.view.profile

import android.Manifest
import com.example.inventoryapplication.R
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.inventoryapplication.databinding.ActivityEditProfileBinding
import com.example.inventoryapplication.models.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import com.example.inventoryapplication.utils.Result // Pastikan import Result yang benar

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var selectedPhotoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        observeViewModel()
        setupListeners()

        profileViewModel.loadUserSessionAndProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeViewModel() {
        profileViewModel.profile.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    binding.userNameEditText.setText(user.username)
                    binding.emailEditText.setText(user.email)
                    binding.emailEditText.isEnabled = false

                    val photoUrl = user.photoProfile
                    if (true){
                        val fullUrl = photoUrl.let {
                            it.let { it1 ->
                                it1?.let { it2 ->
                                    if (it2.startsWith("http")) photoUrl
                                    else "https://fauziewan.my.id/fauziewan.my.id/storage/profile_photos/$photoUrl"
                                }
                            }
                        }

                        Glide.with(this)
                            .load(fullUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.profileImage)
                    }
                }

                is Result.Loading -> {
                    // Tampilkan loading spinner jika perlu
                }

                is Result.Error -> {
                    Toast.makeText(
                        this,
                        "Gagal memuat profil: ${result.exception.message ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        profileViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
        }

        profileViewModel.profileUpdateResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.tvEditPhoto.setOnClickListener { showPictureDialog() }

        binding.tvChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.btnSave.setOnClickListener {
            val username = binding.userNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()

            if (username.isBlank()) {
                Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                profileViewModel.updateProfile(username, email, selectedPhotoFile)
            }
        }
    }

    private fun showPictureDialog() {
        val dialogItems = arrayOf("Pilih dari Galeri", "Ambil dari Kamera")
        AlertDialog.Builder(this)
            .setTitle("Pilih Aksi")
            .setItems(dialogItems) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> checkCameraPermissionAndLaunch()
                }
            }.show()
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            binding.profileImage.setImageBitmap(bitmap)
            selectedPhotoFile = createTempFileFromBitmap(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val bitmap = data.extras?.get("data") as? Bitmap
            bitmap?.let {
                binding.profileImage.setImageBitmap(it)
                selectedPhotoFile = createTempFileFromBitmap(it)
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempFileFromBitmap(bitmap: Bitmap): File {
        val file = File.createTempFile("profile_", ".jpg", cacheDir)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}
