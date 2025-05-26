package com.example.inventoryapplication.view.loans

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.models.Loan
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.launch

class LoansViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loans = MutableLiveData<List<Loan>>()

    private val _filteredLoans = MutableLiveData<List<Loan>>()
    val filteredLoans: LiveData<List<Loan>> get() = _filteredLoans

    fun fetchLoans(token: String, role: String?) {
        Log.d("LoansViewModel", "Fetching assets for role: $role and token: $token")

        if (role.isNullOrBlank()) {
            Log.e("LoansVM", "Unknown role: $role")  // Logging untuk debugging
            return
        }

        viewModelScope.launch {
            try {
                val loanList = userRepository.getLoans(token, role)
                _loans.value = loanList
                _filteredLoans.value = loanList
            } catch (e: Exception) {
                Log.e("LoansVM", "Error: ${e.message}")
            }
        }
    }

    fun filterLoans(query: String?, categories: List<String>?) {
        Log.d("LoansViewModel", "Filtering assets with query: $query and categories: $categories")

        val searchText = query?.trim()?.lowercase().orEmpty()

        val filtered = _loans.value?.filter { loan ->
            val matchesCategory = categories.isNullOrEmpty() || categories.contains("All") ||
                    categories.any { it.equals(loan.inventory.category, ignoreCase = true) }

            val matchesQuery = loan.inventory.name.contains(searchText, ignoreCase = true)

            matchesCategory && matchesQuery
        } ?: emptyList()

        _filteredLoans.value = filtered
    }
}