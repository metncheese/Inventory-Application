package com.example.inventoryapplication.view.home

import androidx.lifecycle.*
import com.example.inventoryapplication.data.response.DashboardData
import com.example.inventoryapplication.data.response.UserData
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: UserRepository) : ViewModel() {

    val profile: LiveData<Result<UserData>> = repository.profileResponse
    val dashboardData: LiveData<Result<DashboardData>> = repository.homeResponse

    fun loadUserSessionAndProfile() {
        viewModelScope.launch {
            val session = repository.getSession().first()
            if (session.token.isNotEmpty()) {
                repository.getProfile(session.token)
                repository.getDashboardData(session.token, session.role ?: "user")
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            repository.refreshDashboard()
        }
    }

    fun getSession(): Flow<UserModel> = repository.getSession()
}
