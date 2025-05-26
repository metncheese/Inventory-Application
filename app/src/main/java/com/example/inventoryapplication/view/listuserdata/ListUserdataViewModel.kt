package com.example.inventoryapplication.view.listuserdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListUserdataViewModel(private val repo: UserRepository) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val session = repo.getSession().first()
                val data = repo.getAllUsers(session.token)
                _users.postValue(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}