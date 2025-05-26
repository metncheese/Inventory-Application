package com.example.inventoryapplication.view.loans

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.ActivityLoanDetailBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoanDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoanDetailBinding
    private lateinit var viewModel: LoanDetailViewModel
    private var currentUserRole: String = "guest"
    private var loanId: Int = -1
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loanId = intent.getIntExtra("loan_id", -1)
        if (loanId == -1) {
            Toast.makeText(this, "ID peminjaman tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this))[LoanDetailViewModel::class.java]

        lifecycleScope.launch {
            val user = viewModel.getUserSession().first()
            token = user.token ?: return@launch
            currentUserRole = user.role ?: "guest"

            observeLoanDetail()
            observeDeleteResult()

            viewModel.fetchLoanDetail(loanId, token, currentUserRole)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeLoanDetail() {
        viewModel.loanDetail.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.itemImage.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.itemImage.visibility = View.VISIBLE

                    val detail = result.data
                    val inventory = detail.inventory
                    val user = detail.user

                    binding.apply {
                        itemName.text = inventory.name
                        fillCategory.text = inventory.category
                        fillBorrowDate.text = detail.start_at.substring(0, 10)
                        fillBorrowerName.text = user.username
                        fillReturnDate.text = detail.expired_at.substring(0, 10)
                        fillTotal.text = detail.quantity

                        Glide.with(this@LoanDetailActivity)
                            .load(inventory.photo_asset ?: R.drawable.ic_placeholder)
                            .into(itemImage)

                        deleteButton.visibility = if (currentUserRole == "admin") View.VISIBLE else View.GONE

                        deleteButton.setOnClickListener {
                            AlertDialog.Builder(this@LoanDetailActivity).apply {
                                setTitle("Konfirmasi Hapus")
                                setMessage("Yakin ingin menghapus data pinjaman ini?")
                                setPositiveButton("Hapus") { _, _ ->
                                    viewModel.deleteLoan(token, loanId)
                                }
                                setNegativeButton("Batal", null)
                                show()
                            }
                        }
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal mengambil data detail aset.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Gagal menghapus data pinjaman", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
