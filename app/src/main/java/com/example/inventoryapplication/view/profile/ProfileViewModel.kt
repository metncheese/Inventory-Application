package com.example.inventoryapplication.view.profile

import androidx.lifecycle.*
import com.example.inventoryapplication.data.response.UserData
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.utils.Result
import com.example.inventoryapplication.utils.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val repo: UserRepository) : ViewModel() {

    val profile: LiveData<Result<UserData>> = repo.profileResponse
    val isLoading: LiveData<Boolean> = repo.isLoading

    private val _profileUpdateResult = MutableLiveData<Boolean>()
    val profileUpdateResult: LiveData<Boolean> = _profileUpdateResult

    private val _passwordChangeResult = MutableLiveData<Boolean>()
    val passwordChangeResult: LiveData<Boolean> = _passwordChangeResult

    private val _userSession = MutableStateFlow<UserModel?>(null)
    val userSession: StateFlow<UserModel?> = _userSession

    fun loadUserSessionAndProfile() {
        viewModelScope.launch {
            try {
                val session = repo.getSession().first()
                _userSession.value = session
                if (session.token.isNotEmpty()) {
                    repo.getProfile(session.token)
                } else {
                    _profileUpdateResult.postValue(false)
                }
            } catch (e: Exception) {
                _profileUpdateResult.postValue(false)
            }
        }
    }

    fun getSession(): StateFlow<UserModel?> {
        return userSession
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repo.clearSession()
            } catch (_: Exception) {
                // Handle failure if needed
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                val token = repo.getSession().first().token
                if (token.isNotEmpty()) {
                    val success = repo.changePassword(token, currentPassword, newPassword, confirmPassword)
                    _passwordChangeResult.postValue(success)
                } else {
                    _passwordChangeResult.postValue(false)
                }
            } catch (e: Exception) {
                _passwordChangeResult.postValue(false)
            }
        }
    }

    fun updateProfile(username: String, email: String, photoFile: File? = null) {
        viewModelScope.launch {
            try {
                val token = repo.getSession().first().token
                if (token.isNotEmpty()) {
                    val success = repo.updateProfile(token, username, email, photoFile)
                    _profileUpdateResult.postValue(success)
                } else {
                    _profileUpdateResult.postValue(false)
                }
            } catch (e: Exception) {
                _profileUpdateResult.postValue(false)
            }
        }
    }
}
