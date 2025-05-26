package com.example.inventoryapplication.view.loans

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.inventoryapplication.R
import com.example.inventoryapplication.data.api.ApiConfig
import com.example.inventoryapplication.databinding.FragmentLoansBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.view.addloan.AddLoanActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoansFragment : Fragment(R.layout.fragment_loans) {

    private var _binding: FragmentLoansBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoansViewModel
    private lateinit var adapter: LoansAdapter
    private lateinit var userRepository: UserRepository

    private val addLoanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == android.app.Activity.RESULT_OK) {
            lifecycleScope.launch {
                val session = userRepository.getSession().first()
                val token = session.token ?: return@launch
                val role = session.role?.trim()?.lowercase() ?: "user"
                viewModel.fetchLoans(token, role)
            }
        }
    }


    private val chipMap = mapOf(
        R.id.chip_all to "All",
        R.id.chip_electronic to "Elektronik",
        R.id.chip_furniture to "furniture",
        R.id.chip_vehicle to "Kendaraan"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        val pref = UserPreference.getInstance(requireContext().dataStore)
        val apiService = ApiConfig.getApiService()
        userRepository = UserRepository.getInstance(pref, apiService)
        val factory = ViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[LoansViewModel::class.java]

        lifecycleScope.launch {
            val session = userRepository.getSession().first()
            val token = session.token
            val role = session.role?.trim()?.lowercase()

            Log.d("LoansFragment", "User role: $role")  // Log role untuk pengecekan

            if (role.isNullOrEmpty()) {
                Log.e("LoansFragment", "Role is missing. Cannot fetch assets.")
                return@launch
            }
            Log.d("LoansFragment", "Proceeding to setup with role: $role")  // Log tambahan untuk cek flow

            setupFab(role)
            setupSearch()
            setupChips()

            // Log token dan role untuk pengecekan lebih lanjut
            Log.d("LoansFragment", "Fetching assets with token: $token and role: $role")

            viewModel.fetchLoans(token, role)

            viewModel.filteredLoans.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)  // uses DiffUtil for efficiency
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val session = userRepository.getSession().first()
            val token = session.token
            val role = session.role?.trim()?.lowercase()
            if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
                viewModel.fetchLoans(token, role)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LoansAdapter() // ✅ assign ke field adapter, bukan bikin variabel lokal
        binding.rvLoans.apply {
            this.adapter = this@LoansFragment.adapter
            setHasFixedSize(true)
        }

        adapter.onItemClick = { loan ->
            Log.d("LoansFragment", "Loan clicked: ${loan.id}")
            val intent = Intent(requireContext(), LoanDetailActivity::class.java)
            intent.putExtra("loan_id", loan.id)
            startActivity(intent)
        }
    }

    private fun setupFab(role: String) {
        binding.floatingAdd.apply {
            visibility = if (role == "admin") View.VISIBLE else View.GONE
            setOnClickListener {
                val intent = Intent(requireContext(), AddLoanActivity::class.java)
                addLoanLauncher.launch(intent)
            }
        }
    }


    private fun setupSearch() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterLoans(newText, getSelectedCategories())
                return true
            }
        })
    }

    private fun setupChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (R.id.chip_all in checkedIds) {
                chipMap.keys.filter { it != R.id.chip_all }.forEach { id ->
                    group.findViewById<View>(id)?.isSelected = false
                }
                group.check(R.id.chip_all)
            } else if (checkedIds.isEmpty()) {
                group.check(R.id.chip_all)
            }

            val selectedCategories = getSelectedCategories()
            viewModel.filterLoans(binding.searchBar.query.toString(), selectedCategories)
        }

        binding.filterChipGroup.check(R.id.chip_all)
    }

    private fun getSelectedCategories(): List<String> {
        val checkedIds = binding.filterChipGroup.checkedChipIds
        return checkedIds.mapNotNull { chipMap[it] }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}