package com.example.inventoryapplication.view.loans

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.data.response.LoanDetailData
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoanDetailViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loanDetail = MutableLiveData<Result<LoanDetailData>>() // Ganti LoanData ke LoanDetailData
    val loanDetail: LiveData<Result<LoanDetailData>> = _loanDetail

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

    fun fetchLoanDetail(id: Int, token: String, role: String) {
        _loanDetail.postValue(Result.Loading)
        viewModelScope.launch {
            try {
                val loanDetailResponse = when (role.lowercase()) {
                    "admin" -> repository.getAdminLoanDetail(token, id)
                    else -> repository.getUserLoanDetail(token,id)
                }
                _loanDetail.postValue(Result.Success(loanDetailResponse.data))
            } catch (e: Exception) {
                _loanDetail.postValue(Result.Error(e))
                Log.e("LoanDetailViewModel", "Error fetching asset detail", e)
            }
        }
    }

    fun getUserSession(): Flow<UserModel> = repository.getSession()


    fun deleteLoan(token: String, loanId: Int) {
        _deleteResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteLoan(token, loanId)
                if (response.isSuccessful) {
                    _deleteResult.value = Result.Success(response.body()?.message ?: "Peminjaman berhasil dihapus")
                } else {
                    _deleteResult.value = Result.Error(Exception("Gagal menghapus peminjaman"))
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.Error(e)
            }
        }
    }
}