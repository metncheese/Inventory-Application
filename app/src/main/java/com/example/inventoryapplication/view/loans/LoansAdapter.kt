package com.example.inventoryapplication.view.loans

import com.example.inventoryapplication.databinding.ItemLoanBinding
import com.example.inventoryapplication.models.Loans
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LoansAdapter : ListAdapter<Loans, LoansAdapter.LoansViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoansViewHolder {
        val binding = ItemLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoansViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoansViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LoansViewHolder(private val binding: ItemLoanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loans: Loans) {
            binding.tvNamaBarang.text = loans.name
            binding.tvNamaPeminjam.text = loans.user
            binding.fillCategory.text = loans.category
            binding.fillEntryDate.text = loans.date
            binding.fillPosition.text = loans.location
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Loans>() {
            override fun areItemsTheSame(oldItem: Loans, newItem: Loans): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Loans, newItem: Loans): Boolean {
                return oldItem == newItem
            }
        }
    }
}
