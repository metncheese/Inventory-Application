package com.example.inventoryapplication.view.assets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventoryapplication.R
import com.bumptech.glide.Glide
import com.example.inventoryapplication.databinding.ItemAssetBinding
import com.example.inventoryapplication.models.Asset

class AssetsAdapter : ListAdapter<Asset, AssetsAdapter.AssetViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Asset) -> Unit)? = null

    inner class AssetViewHolder(private val binding: ItemAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(asset: Asset) {
            binding.apply {
                tvNamaBarang.text = asset.name
                fillTotal.text = asset.quantity.toString()
                fillEntryDate.text = asset.asset_entry
                fillPlacement.text = asset.placement

                val imageUrl = asset.photo_asset
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imageBarang)
                } else {
                    Glide.with(root.context)
                        .load(R.drawable.ic_placeholder)
                        .into(imageBarang)
                }

                root.setOnClickListener {
                    onItemClick?.invoke(asset)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = getItem(position)
        holder.bind(asset)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Asset>() {
            override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem == newItem
            }
        }
    }
}
