package com.example.inventoryapplication.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.FragmentProfileBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.view.listuserdata.ListUserDataActivity
import com.example.inventoryapplication.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        observeProfile()
        setupListeners()
        viewModel.loadUserSessionAndProfile()
    }

    override fun onResume() {
        super.onResume()
        observeProfile()
        setupListeners()
        viewModel.loadUserSessionAndProfile()
    }

    private fun observeProfile() {
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    // Optional: Show progress bar
                }

                is Result.Success -> {
                    val user = result.data
                    binding.tvUsername.text = user.username
                    binding.tvEmail.text = user.email

                    viewModel.getSession().asLiveData().observe(viewLifecycleOwner) { session ->

                        val fullPhotoUrl = user.photoProfile?.let { photo ->
                            if (photo.startsWith("http")) photo
                            else "https://fauziewan.my.id/fauziewan.my.id/storage/profile_photos/$photo"
                        }

                        Glide.with(this@ProfileFragment)
                            .load(fullPhotoUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.ivProfile)
                    }

                    // Show admin-only section
                    binding.tvListUserData.visibility =
                        if (user.role == "admin") View.VISIBLE else View.GONE
                }

                is Result.Error -> {
                    Log.e("ProfileFragment", "Error loading profile", result.exception)
                    // Optional: Show error to user via Toast/Snack bar
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.tvListUserData.setOnClickListener {
            startActivity(Intent(requireContext(), ListUserDataActivity::class.java))
        }

        binding.tvLogout.setOnClickListener {
            lifecycleScope.launch {
                viewModel.logout()
                startActivity(Intent(requireContext(), WelcomeActivity::class.java))
                requireActivity().finishAffinity()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}