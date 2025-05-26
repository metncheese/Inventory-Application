package com.example.inventoryapplication.view.loans

import android.annotation.SuppressLint
import com.example.inventoryapplication.databinding.ItemLoanBinding
import com.example.inventoryapplication.models.Loan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LoansAdapter : ListAdapter<Loan, LoansAdapter.LoanViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Loan) -> Unit)? = null // ✅ Perbaikan tipe huruf kecil jadi besar di Unit

    inner class LoanViewHolder(private val binding: ItemLoanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(loan: Loan) {
            val user = loan.user
            val asset = loan.inventory

            binding.apply {
                tvNamaBarang.text = asset.name
                fillBorrowerName.text = user.username
                fillBorrowingDate.text = loan.startAt.take(10)  // ✅ ambil yyyy-MM-dd
                fillExpDate.text = loan.expiredAt.take(10)

                Glide.with(root.context)
                    .load(asset.photo_asset)
                    .into(imageBarang)

                root.setOnClickListener {
                    onItemClick?.invoke(loan) // ✅ invoke loan, bukan asset
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val binding = ItemLoanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = getItem(position)
        holder.bind(loan)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Loan>() {
            override fun areItemsTheSame(oldItem: Loan, newItem: Loan): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Loan, newItem: Loan): Boolean {
                return oldItem == newItem
            }
        }
    }
}
