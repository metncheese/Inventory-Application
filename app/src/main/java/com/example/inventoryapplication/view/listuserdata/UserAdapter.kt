package com.example.inventoryapplication.view.listuserdata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.inventoryapplication.R
import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.databinding.ItemDataStaffBinding
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(private val onClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val userList = mutableListOf<User>()

    fun submitList(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemDataStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(private val binding: ItemDataStaffBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvNamaBarang.text = user.username
            binding.fillEmail.text = user.email

            val formattedDate = user.createdAt?.let {
                try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    formatter.format(parser.parse(it) ?: Date())
                } catch (e: Exception) {
                    "-"
                }
            } ?: "-"

            val photoUrl = if (!user.photoProfile.isNullOrBlank()) {
                if (user.photoProfile.startsWith("http")) user.photoProfile
                else "https://fauziewan.my.id/storage/${user.photoProfile.trimStart('/')}"
            } else null

            Glide.with(binding.imageUser.context)
                .load(photoUrl ?: R.drawable.ic_profile_placeholder)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(binding.imageUser)

            binding.root.setOnClickListener {
                onClick(user)
            }
        }
    }
}
