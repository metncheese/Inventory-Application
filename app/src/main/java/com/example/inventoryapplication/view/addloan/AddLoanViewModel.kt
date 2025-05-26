package com.example.inventoryapplication.view.addloan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.data.response.AddLoanResponse
import com.example.inventoryapplication.models.Asset
import com.example.inventoryapplication.utils.AddLoanRequest
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log

class AddLoanViewModel(private val repository: UserRepository) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val availableAssets = MutableLiveData<List<Asset>>()
    val addLoanResult = MutableLiveData<Result<AddLoanResponse>>()

    fun fetchLoanableAssets(token: String, role: String) {
        viewModelScope.launch {
            try {
                val allAssets = repository.getAssets(token, role)
                val filtered = allAssets.filter { it.is_can_loan }
                availableAssets.postValue(filtered)
            } catch (e: Exception) {
                Log.e("AddLoanViewModel", "Error fetchLoanableAssets: ${e.message}", e)
            }
        }
    }

    fun addLoan(token: String, request: AddLoanRequest) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val response = repository.postAddLoan(token, request)
                addLoanResult.postValue(Result.Success(response))
            } catch (e: Exception) {
                addLoanResult.postValue(Result.Error(e))
                Log.e("AddLoanViewModel", "Error addLoan: ${e.message}", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    suspend fun getAllUsers(token: String): List<User> {
        return try {
            Log.d("AddLoanViewModel", "Calling getAllUsers with token: $token")
            val users = repository.getAllUsers(token)
            Log.d("AddLoanViewModel", "getAllUsers success: total users = ${users.size}")
            users
        } catch (e: Exception) {
            Log.e("AddLoanViewModel", "Error in getAllUsers: ${e.message}", e)
            emptyList()
        }
    }

    fun getSession() = repository.getSession()
}
