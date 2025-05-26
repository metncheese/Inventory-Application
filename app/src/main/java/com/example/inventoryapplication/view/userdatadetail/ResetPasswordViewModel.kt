package com.example.inventoryapplication.view.userdatadetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val repository: UserRepository) : ViewModel() {

    private val _resetPasswordResult = MutableLiveData<Result<String>>()
    val resetPasswordResult: LiveData<Result<String>> = _resetPasswordResult

    fun resetPassword(userId: Int, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordResult.value = Result.Loading
            try {
                val session: UserModel = repository.getSession().first()
                Log.d("ResetPasswordViewModel", "Mengirim reset password userId=$userId")
                repository.resetUserPassword(session.token, userId, newPassword)
                _resetPasswordResult.value = Result.Success("Password berhasil diperbarui")
            } catch (e: Exception) {
                Log.e("ResetPasswordViewModel", "Error reset password", e)
                _resetPasswordResult.value = Result.Error(e)
            }
        }
    }
}
