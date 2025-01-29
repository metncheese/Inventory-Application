package com.example.inventoryapplication.view.assets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.inventoryapplication.R
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapplication.databinding.FragmentAssetsBinding
import com.example.inventoryapplication.models.Asset

class AssetsFragment : Fragment() {

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var assetAdapter: AssetAdapter
    private var assetList: List<Asset> = listOf()
    private var filteredList: List<Asset> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assetAdapter = AssetAdapter()
        binding.rvAssets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }

        // Dummy data
        assetList = listOf(
            Asset("Laptop", "Device", "24 Agustus 2024", "Ruangan HRD"),
            Asset("Mouse", "Device", "28 Mei 2024", "Ruangan Direksi"),
            Asset("Notebook", "Device", "11 Januari 2024", "Ruangan Manager"),
            Asset("Printer", "Device", "21 Februari 2024", "Ruangan Cetak"),
            Asset("Projector", "Device", "14 Oktober 2023", "Ruangan Meeting"),
            Asset("Pen", "Stationery", "21 Juli 2023", "Ruangan Karyawan"),
            Asset("Chair", "Furniture", "01 maret 2022", "Ruangan Tamu")
        )

        filteredList = assetList
        assetAdapter.submitList(filteredList)

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

        filteredList = assetList.filter {
            (category == null || it.category == category) &&
                    (query.isNullOrEmpty() || it.name.lowercase().contains(lowerCaseQuery))
        }
        assetAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

