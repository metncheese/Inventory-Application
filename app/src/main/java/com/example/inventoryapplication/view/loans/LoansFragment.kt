package com.example.inventoryapplication.view.loans

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.FragmentLoansBinding
import com.example.inventoryapplication.models.Loans

class LoansFragment : Fragment() {

    private var _binding: FragmentLoansBinding? = null
    private val binding get() = _binding!!
    private lateinit var loansAdapter: LoansAdapter
    private var loansList: List<Loans> = listOf()
    private var filteredList: List<Loans> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoansBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loansAdapter = LoansAdapter()
        binding.rvLoans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = loansAdapter
        }

        // Dummy data
        loansList = listOf(
            Loans("Laptop", "Plenger Jeding", "Device", "24 Agustus 2024", "Ruangan HRD"),
            Loans("Mouse", "Plenger Jeding", "Device", "28 Mei 2024", "Ruangan Direksi"),
            Loans("Notebook", "Plenger Jeding", "Device", "11 Januari 2024", "Ruangan Manager"),
            Loans("Printer", "Plenger Jeding", "Device", "21 Februari 2024", "Ruangan Cetak"),
            Loans("Projector", "Plenger Jeding", "Device", "14 Oktober 2023", "Ruangan Meeting"),
            Loans("Pen", "Plenger Jeding", "Stationery", "21 Juli 2023", "Ruangan Karyawan"),
            Loans("Chair", "Plenger Jeding", "Furniture", "01 maret 2022", "Ruangan Tamu")
        )

        filteredList = loansList
        loansAdapter.submitList(filteredList)

        setupSearchView()
        setupChipGroup()
    }

    private fun setupSearchView() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun setupChipGroup() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val category = when {
                checkedIds.contains(R.id.chip_all) -> null
                checkedIds.contains(R.id.chip_device) -> "Device"
                checkedIds.contains(R.id.chip_stationery) -> "Stationery"
                checkedIds.contains(R.id.chip_loan) -> "Loan"
                else -> null
            }
            filterList(binding.searchBar.query.toString(), category)
        }
    }

    private fun filterList(query: String?, category: String? = null) {
        val lowerCaseQuery = query?.lowercase() ?: ""

        filteredList = loansList.filter {
            (category == null || it.category == category) &&
                    (query.isNullOrEmpty() || it.name.lowercase().contains(lowerCaseQuery))
        }
        loansAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}