package com.example.inventoryapplication.view.assets

import android.util.Log
import androidx.lifecycle.*
import com.example.inventoryapplication.models.Asset
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.launch

class AssetsViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _assets = MutableLiveData<List<Asset>>()
    private val _filteredAssets = MutableLiveData<List<Asset>>()
    val filteredAssets: LiveData<List<Asset>> get() = _filteredAssets

    private var lastQuery: String? = null
    private var lastCategories: List<String>? = null
    private var selectedOwnership: String? = null

    fun fetchAssets(token: String, role: String?) {
        Log.d("AssetsViewModel", "Fetching assets for role: $role and token: $token")

        if (role.isNullOrBlank()) {
            Log.e("AssetsVM", "Unknown role: $role")
            return
        }

        viewModelScope.launch {
            try {
                val assetList = userRepository.getAssets(token, role)
                Log.d("AssetsViewModel", "Asset category values: ${'$'}{assetList.map { it.category }}")
                _assets.value = assetList
                applyAllFilters()
            } catch (e: Exception) {
                Log.e("AssetsVM", "Error: ${'$'}{e.message}")
            }
        }
    }

    fun updateSearchAndCategory(query: String?, categories: List<String>?) {
        lastQuery = query
        lastCategories = categories
        applyAllFilters()
    }

    fun applyOwnershipAPI(token: String, role: String, filter: String) {
        viewModelScope.launch {
            try {
                val filteredAssets = userRepository.getFilteredAssets(token, role, filter)
                _assets.value = filteredAssets
                applyAllFilters()
            } catch (e: Exception) {
                Log.e("AssetsViewModel", "Failed API filter: ${e.message}")
            }
        }
    }

    fun setOwnershipFilter(ownership: String?) {
        selectedOwnership = ownership
        applyAllFilters()
    }

    private fun applyAllFilters() {
        val searchText = lastQuery?.trim()?.lowercase().orEmpty()
        val categories = lastCategories ?: emptyList()
        val assets = _assets.value ?: return

        val filtered = assets.filter { asset ->
            val matchesCategory = categories.isEmpty() || categories.contains("All") ||
                    categories.any { it.equals(asset.category, ignoreCase = true) }

            val matchesQuery = asset.name.contains(searchText, ignoreCase = true)

            val matchesOwnership = selectedOwnership == null || asset.category == selectedOwnership

            matchesCategory && matchesQuery && matchesOwnership
        }

        _filteredAssets.value = filtered
    }
}
