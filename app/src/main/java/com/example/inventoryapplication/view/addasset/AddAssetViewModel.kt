package com.example.inventoryapplication.view.addasset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.models.Asset
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.launch

class AddAssetViewModel(val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _addAssetResult = MutableLiveData<Result<Asset>>()
    val addAssetResult: LiveData<Result<Asset>> get() = _addAssetResult

    fun addAssetWithPhoto(
        token: String,
        name: String,
        category: String,
        placement: String,
        assetEntry: String,
        expiredDate: String,
        isCanLoan: Boolean,
        invStatus: String,
        quantity: Int,
        imageFile: java.io.File?
    ) {
        _isLoading.value = true
        _addAssetResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val result = repository.postAddAssetWithPhoto(
                    token,
                    name,
                    category,
                    placement,
                    assetEntry,
                    expiredDate,
                    isCanLoan,
                    invStatus,
                    quantity,
                    imageFile
                )
                _addAssetResult.value = Result.Success(result)
            } catch (e: Exception) {
                _addAssetResult.value = Result.Error(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

}