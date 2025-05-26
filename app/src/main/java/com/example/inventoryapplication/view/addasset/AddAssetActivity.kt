package com.example.inventoryapplication.view.addasset

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.ActivityAddAssetBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddAssetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAssetBinding
    private val viewModel: AddAssetViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var imageUri: Uri? = null
    private lateinit var cameraImageFile: File
    private var cameraImageUri: Uri? = null
    private var quantity = 1

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).placeholder(R.drawable.ic_placeholder).into(binding.imageBarang)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = cameraImageUri
            cameraImageUri?.let {
                Glide.with(this).load(it).placeholder(R.drawable.ic_placeholder).into(binding.imageBarang)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddAssetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Glide.with(this).load(R.drawable.ic_placeholder).into(binding.imageBarang)

        setupDropdowns()
        setupPhotoChooser()
        setupQuantityButtons()
        setupToolbarNavigation()

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog(binding.dateEditText)
        }
        binding.returnDateEditText.setOnClickListener {
            showDatePickerDialog(binding.returnDateEditText)
        }
        binding.dateEditText.inputType = InputType.TYPE_NULL
        binding.returnDateEditText.inputType = InputType.TYPE_NULL

        binding.btnSave.setOnClickListener {
            addAsset()
        }

        viewModel.addAssetResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    Toast.makeText(this, "Asset berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Gagal: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            editText.setText(dateFormat.format(calendar.time))
        }
        DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupDropdowns() {
        val ownershipOptions = listOf("Perusahaan", "Pinjaman")
        val statusOptions = listOf("Bisa dipinjam", "Tidak bisa dipinjam")

        val ownershipAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ownershipOptions)
        binding.ownershipDropdown.setAdapter(ownershipAdapter)

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions)
        binding.assetStatusDropdown.setAdapter(statusAdapter)
    }

    private fun setupPhotoChooser() {
        binding.tvAddPhoto.setOnClickListener {
            val options = arrayOf("Kamera", "Galeri")
            AlertDialog.Builder(this)
                .setTitle("Pilih Gambar")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            cameraImageFile = File.createTempFile("asset_photo_", ".jpg", cacheDir)
                            cameraImageUri = FileProvider.getUriForFile(
                                this,
                                "${packageName}.provider",
                                cameraImageFile
                            )
                            cameraImageUri?.let { takePictureLauncher.launch(it) }
                        }
                        1 -> {
                            pickImage.launch("image/*")
                        }
                    }
                }.show()
        }
    }

    private fun setupQuantityButtons() {
        binding.tvQuantity.text = quantity.toString()

        binding.btnIncrement.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        binding.btnDecrement.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }
    }

    private fun setupToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun addAsset() {
        val name = binding.stuffNameEditText.text.toString()
        val category = binding.categoryEditText.text.toString()
        val placement = binding.placementEditText.text.toString()

        val assetEntry = binding.dateEditText.text.toString()
            .takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: ""

        val expiredDateRaw = binding.returnDateEditText.text.toString()
        val expiredDate = expiredDateRaw
            .takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: ""

        val isCanLoan = binding.assetStatusDropdown.text.toString() == "Bisa dipinjam"

        val invStatus = when (binding.ownershipDropdown.text.toString()) {
            "Perusahaan" -> "owned"
            "Pinjaman" -> "borrowed"
            else -> "owned"
        }

        val imageFile = imageUri?.let { uriToFile(it) }

        MainScope().launch {
            val token = viewModel.repository.getSession().first().token
            try {
                val result = viewModel.repository.postAddAssetWithPhoto(
                    token = token ?: return@launch,
                    name = name,
                    category = category,
                    placement = placement,
                    assetEntry = assetEntry,
                    expiredDate = expiredDate,
                    isCanLoan = isCanLoan,
                    invStatus = invStatus,
                    quantity = quantity,
                    imageFile = imageFile
                )
                Toast.makeText(this@AddAssetActivity, "Asset berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddAssetActivity, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
