package com.example.inventoryapplication.view.signin

import android.util.Log
import androidx.lifecycle.*
import com.example.inventoryapplication.data.response.SignInResponse
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.launch

class SignInViewModel(private val repository: UserRepository) : ViewModel() {

    private val _signInResult = MutableLiveData<Result<SignInResponse>>()
    val signInResult: LiveData<Result<SignInResponse>> = _signInResult

    val isLoading: LiveData<Boolean> = repository.isLoading

    fun postSignIn(email: String, password: String) {
        viewModelScope.launch {
            _signInResult.postValue(Result.Loading)
            try {
                val response = repository.postSignIn(email, password)
                _signInResult.postValue(Result.Success(response))
                Log.d("SignInViewModel", "✅ Login success for ${response.user.username}")
            } catch (e: Exception) {
                _signInResult.postValue(Result.Error(e))
                Log.e("SignInViewModel", "❌ Login failed", e)
            }
        }
    }

    fun getUserSession() = repository.getSession()
}
