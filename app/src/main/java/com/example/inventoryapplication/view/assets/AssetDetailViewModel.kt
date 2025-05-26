package com.example.inventoryapplication.view.assets

import android.util.Log
import androidx.lifecycle.*
import com.example.inventoryapplication.data.response.AssetData
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AssetDetailViewModel(private val repository: UserRepository) : ViewModel() {

    private val _assetDetail = MutableLiveData<Result<AssetData>>()
    val assetDetail: LiveData<Result<AssetData>> = _assetDetail

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

    fun fetchAssetDetail(id: Int, token: String, role: String) {
        _assetDetail.postValue(Result.Loading)
        viewModelScope.launch {
            try {
                val detailResponse = when (role.lowercase()) {
                    "admin" -> repository.getAdminAssetDetail(token, id)
                    else -> repository.getUserAssetDetail(token, id)
                }
                _assetDetail.postValue(Result.Success(detailResponse.data))
            } catch (e: Exception) {
                _assetDetail.postValue(Result.Error(e))
                Log.e("AssetDetailViewModel", "Error fetching asset detail", e)
            }
        }
    }

    fun getUserSession(): Flow<UserModel> = repository.getSession()

    fun deleteAsset(token: String, assetId: Int) {
        _deleteResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteAsset(token, assetId)
                if (response.isSuccessful) {
                    _deleteResult.value = Result.Success(response.body()?.message ?: "Berhasil menghapus")
                } else {
                    _deleteResult.value = Result.Error(Exception("Gagal menghapus aset"))
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.Error(e)
            }
        }
    }
}
