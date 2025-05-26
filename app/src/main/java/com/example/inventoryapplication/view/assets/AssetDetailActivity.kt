package com.example.inventoryapplication.view.assets

import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlertDialog
import androidx.activity.result.ActivityResult
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.ActivityAssetDetailBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AssetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssetDetailBinding
    private lateinit var viewModel: AssetDetailViewModel
    private var assetId: Int = -1
    private var token: String = ""

    private val editAssetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    viewModel.fetchAssetDetail(assetId, token, "admin") // ganti role sesuai sesi
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        assetId = intent.getIntExtra("asset_id", -1)
        if (assetId == -1) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this))[AssetDetailViewModel::class.java]

        lifecycleScope.launch {
            val user = viewModel.getUserSession().first()
            token = user.token ?: return@launch
            val role = user.role ?: "guest"

            observeAssetDetail(role)
            observeDeleteResult()

            viewModel.fetchAssetDetail(assetId, token, role)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeAssetDetail(role: String) {
        viewModel.assetDetail.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.itemImage.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.itemImage.visibility = View.VISIBLE

                    val detail = result.data
                    binding.apply {
                        itemName.text = detail.name
                        fillCategory.text = detail.category
                        fillPlacement.text = detail.placement
                        fillEntryDate.text = detail.asset_entry?.substring(0, 10) ?: "-"
                        fillReturnDate.text = detail.expired_date?.substring(0, 10) ?: "-"
                        fillOwnership.text = detail.inv_status
                        fillAssetStatus.text = if (detail.is_can_loan) "Bisa dipinjam" else "Tidak bisa dipinjam"
                        fillTotal.text = detail.quantity

                        Glide.with(this@AssetDetailActivity)
                            .load(detail.img_url ?: R.drawable.ic_placeholder)
                            .into(itemImage)

                        if (role != "admin") {
                            editButton.visibility = View.GONE
                            deleteButton.visibility = View.GONE
                        }

                        editButton.setOnClickListener {
                            val intent = Intent(this@AssetDetailActivity, EditAssetActivity::class.java)
                            intent.putExtra("asset_id", detail.id)
                            editAssetLauncher.launch(intent)
                        }

                        deleteButton.setOnClickListener {
                            AlertDialog.Builder(this@AssetDetailActivity).apply {
                                setTitle("Konfirmasi Hapus")
                                setMessage("Yakin ingin menghapus aset ini?")
                                setPositiveButton("Hapus") { _, _ ->
                                    viewModel.deleteAsset(token, detail.id)
                                }
                                setNegativeButton("Batal", null)
                                show()
                            }
                        }
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal memuat detail asset", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun observeDeleteResult() {
        viewModel.deleteResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal menghapus aset", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
