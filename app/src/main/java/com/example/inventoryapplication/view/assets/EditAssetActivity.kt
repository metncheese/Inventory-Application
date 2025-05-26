package com.example.inventoryapplication.view.assets

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.inventoryapplication.databinding.ActivityEditAssetBinding
import com.example.inventoryapplication.models.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.inventoryapplication.R
import com.example.inventoryapplication.utils.Result

@AndroidEntryPoint
class EditAssetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAssetBinding
    private lateinit var viewModel: EditAssetViewModel
    private var assetId: Int = -1
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAssetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this))[EditAssetViewModel::class.java]

        assetId = intent.getIntExtra("asset_id", -1)
        if (assetId == -1) {
            Toast.makeText(this, "ID aset tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val session = viewModel.getSession()
            token = session.token ?: ""
            loadAssetDetail()
        }

        setupDatePickers()
        setupDropdowns()
        binding.btnSave.setOnClickListener { submitEdit() }
    }

    private fun loadAssetDetail() {
        viewModel.getAssetDetail(assetId, token).observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val asset = result.data
                    Glide.with(this)
                        .load(if (asset.img_url?.startsWith("http") == true) asset.img_url else "https://fauziewan.my.id/${asset.img_url}")
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(binding.assetImg)

                    binding.assetName.text = asset.name
                    binding.categoryEditText.setText(asset.category)
                    binding.entryDateEditText.setText(asset.asset_entry)
                    binding.placementEditText.setText(asset.placement)
                    binding.ownershipDropdown.setText(asset.inv_status, false)
                    binding.assetStatusDropdown.setText(
                        if (asset.is_can_loan) "Bisa dipinjam" else "Tidak bisa dipinjam", false
                    )
                    binding.returnDateEditText.setText(asset.expired_date)
                    binding.totalEditText.setText(asset.quantity)
                }
                is Result.Error -> {
                    Toast.makeText(this, "Gagal load data: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Result.Loading -> {}
            }
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        listOf(binding.entryDateEditText, binding.returnDateEditText).forEach { field ->
            field.setOnClickListener {
                DatePickerDialog(this, { _, y, m, d ->
                    field.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
    }

    private fun setupDropdowns() {
        val ownershipList = listOf("perusahaan", "pinjaman")
        val assetStatusList = listOf("Bisa dipinjam", "Tidak bisa dipinjam")

        binding.ownershipDropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ownershipList))
        binding.assetStatusDropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assetStatusList))
    }

    private fun submitEdit() {
        val entryDate = binding.entryDateEditText.text.toString()
        val placement = binding.placementEditText.text.toString()
        val ownership = binding.ownershipDropdown.text.toString()
        val status = binding.assetStatusDropdown.text.toString()
        val returnDate = binding.returnDateEditText.text.toString()
        val quantity = binding.totalEditText.text.toString().toIntOrNull() ?: 1
        val isCanLoan = status == "Bisa dipinjam"

        viewModel.updateAsset(
            assetId, token, entryDate, placement, ownership, isCanLoan,
            returnDate.ifBlank { null }, quantity
        ).observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Gagal update: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                Result.Loading -> {}
            }
        }
    }
}
