package com.example.inventoryapplication.di

import android.content.Context
import com.example.inventoryapplication.data.api.ApiConfig
import com.example.inventoryapplication.pref.UserPreference
import com.example.inventoryapplication.pref.dataStore
import com.example.inventoryapplication.utils.UserRepository

object Injection {
    fun provideRepository (context : Context): UserRepository {
        val apiService = ApiConfig.getApiService()
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref, apiService)
    }
}