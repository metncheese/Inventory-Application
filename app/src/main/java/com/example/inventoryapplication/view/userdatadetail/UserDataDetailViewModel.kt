package com.example.inventoryapplication.view.userdatadetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.launch

class UserDataDetailViewModel(private val repo: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun fetchUserDetail(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                val result = repo.getUserById(token, userId)
                if (result.role.lowercase() == "user") {
                    _user.postValue(result)
                } else {
                    _user.postValue(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _user.postValue(null)
            }
        }
    }
}
