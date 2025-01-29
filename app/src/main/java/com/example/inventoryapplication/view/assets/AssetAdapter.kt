package com.example.inventoryapplication.view.assets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventoryapplication.databinding.ItemAssetBinding
import com.example.inventoryapplication.models.Asset

class AssetAdapter : ListAdapter<Asset, AssetAdapter.AssetViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AssetViewHolder(private val binding: ItemAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(asset: Asset) {
            binding.tvNamaBarang.text = asset.name
            binding.fillCategory.text = asset.category
            binding.fillEntryDate.text = asset.date
            binding.fillPosition.text = asset.location
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Asset>() {
            override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem == newItem
            }
        }
    }
}
