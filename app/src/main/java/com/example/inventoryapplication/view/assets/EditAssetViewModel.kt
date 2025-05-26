package com.example.inventoryapplication.view.assets

import androidx.lifecycle.*
import com.example.inventoryapplication.models.Asset
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditAssetViewModel(private val repo: UserRepository) : ViewModel() {

    private val _assetDetail = MutableLiveData<Result<Asset>>()
    val assetDetail: LiveData<Result<Asset>> = _assetDetail

    fun getAssetDetail(id: Int, token: String): LiveData<Result<com.example.inventoryapplication.data.response.AssetData>> {
        val result = MutableLiveData<Result<com.example.inventoryapplication.data.response.AssetData>>()
        result.value = Result.Loading
        viewModelScope.launch {
            try {
                val response = repo.getAdminAssetDetail(token, id)
                result.postValue(Result.Success(response.data))
            } catch (e: Exception) {
                result.postValue(Result.Error(e))
            }
        }
        return result
    }

    fun updateAsset(
        id: Int,
        token: String,
        entryDate: String,
        placement: String,
        invStatus: String,
        isCanLoan: Boolean,
        expiredDate: String?,
        quantity: Int
    ): LiveData<Result<String>> {
        val resultLiveData = MutableLiveData<Result<String>>()
        resultLiveData.value = Result.Loading
        viewModelScope.launch {
            try {
                val message = repo.editAsset(token, id, entryDate, placement, invStatus, isCanLoan, expiredDate, quantity)
                resultLiveData.postValue(Result.Success(message))
            } catch (e: Exception) {
                resultLiveData.postValue(Result.Error(e))
            }
        }
        return resultLiveData
    }

    suspend fun getSession(): UserModel {
        return repo.getSession().first()
    }
}
