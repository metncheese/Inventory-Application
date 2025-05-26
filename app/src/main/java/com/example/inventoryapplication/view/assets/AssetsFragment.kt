package com.example.inventoryapplication.view.assets

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapplication.R
import com.example.inventoryapplication.data.api.ApiConfig
import com.example.inventoryapplication.databinding.FragmentAssetsBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.view.addasset.AddAssetActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AssetsFragment : Fragment(R.layout.fragment_assets) {

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AssetsViewModel
    private lateinit var adapter: AssetsAdapter

    private lateinit var userRepository: UserRepository

    private val chipMap = mapOf(
        R.id.chip_all to "All",
        R.id.chip_electronic to "Elektronik",
        R.id.chip_furniture to "Furniture",
        R.id.chip_vehicle to "Kendaraan"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        val pref = UserPreference.getInstance(requireContext().dataStore)
        val apiService = ApiConfig.getApiService()
        userRepository = UserRepository.getInstance(pref, apiService)
        val factory = ViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[AssetsViewModel::class.java]

        lifecycleScope.launch {
            val session = userRepository.getSession().first()
            val token = session.token
            val role = session.role?.trim()?.lowercase()

            Log.d("AssetsFragment", "User role: $role")

            if (role.isNullOrEmpty()) {
                Log.e("AssetsFragment", "Role is missing. Cannot fetch assets.")
                return@launch
            }
            Log.d("AssetsFragment", "Proceeding to setup with role: $role")

            setupFab(role)
            setupSearch()
            setupChips()
            setupOwnershipFilter()

            Log.d("AssetsFragment", "Fetching assets with token: $token and role: $role")

            viewModel.fetchAssets(token, role)

            viewModel.filteredAssets.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
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
                viewModel.fetchAssets(token, role)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AssetsAdapter()
        binding.rvAssets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AssetsFragment.adapter
            setHasFixedSize(true)
        }

        adapter.onItemClick = { asset ->
            val intent = Intent(requireContext(), AssetDetailActivity::class.java)
            intent.putExtra("asset_id", asset.id)
            startActivity(intent)
        }
    }

    private fun setupFab(role: String) {
        binding.floatingAdd.apply {
            visibility = if (role == "admin") View.VISIBLE else View.GONE
            setOnClickListener {
                startActivity(Intent(requireContext(), AddAssetActivity::class.java))
            }
        }
    }

    private fun setupSearch() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchAndCategory(newText, getSelectedCategories())
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
            viewModel.updateSearchAndCategory(binding.searchBar.query.toString(), selectedCategories)
        }

        binding.filterChipGroup.check(R.id.chip_all)
    }

    private fun setupOwnershipFilter() {
        binding.filterButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.menuInflater.inflate(R.menu.menu_filter_ownership, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                lifecycleScope.launch {
                    val session = userRepository.getSession().first()
                    val token = session.token
                    val role = session.role?.lowercase() ?: "user" // default user
                    when (menuItem.itemId) {
                        R.id.menu_all -> {
                            viewModel.fetchAssets(token, role)
                        }
                        R.id.menu_owned -> {
                            viewModel.applyOwnershipAPI(token, role, "owned")
                        }
                        R.id.menu_borrowed -> {
                            viewModel.applyOwnershipAPI(token, role, "loan")
                        }
                    }
                    true
                }
                false
            }
            popup.show()
        }
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
