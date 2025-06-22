package com.example.inventoryapplication.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.inventoryapplication.data.response.SignUpResponse
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SignUpViewModel(private val repository: UserRepository) : ViewModel() {

    val signUpResult: LiveData<Result<SignUpResponse>> = repository.signUpResponse

    fun postSignUp(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            repository.postSignUp(username, email, password, confirmPassword)
        }
    }
}


