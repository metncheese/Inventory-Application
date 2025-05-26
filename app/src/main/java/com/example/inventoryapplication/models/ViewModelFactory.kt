package com.example.inventoryapplication.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventoryapplication.di.Injection
import com.example.inventoryapplication.utils.UserRepository
import com.example.inventoryapplication.view.addasset.AddAssetViewModel
import com.example.inventoryapplication.view.addloan.AddLoanViewModel
import com.example.inventoryapplication.view.assets.AssetDetailViewModel
import com.example.inventoryapplication.view.assets.AssetsViewModel
import com.example.inventoryapplication.view.assets.EditAssetViewModel
import com.example.inventoryapplication.view.signin.SignInViewModel
import com.example.inventoryapplication.view.signup.SignUpViewModel
import com.example.inventoryapplication.view.home.HomeViewModel
import com.example.inventoryapplication.view.listuserdata.ListUserdataViewModel
import com.example.inventoryapplication.view.loans.LoanDetailViewModel
import com.example.inventoryapplication.view.loans.LoansViewModel
import com.example.inventoryapplication.view.profile.ProfileViewModel
import com.example.inventoryapplication.view.userdatadetail.ResetPasswordViewModel
import com.example.inventoryapplication.view.userdatadetail.UserDataDetailViewModel

/**
 * Factory class untuk membuat instance ViewModel dengan constructor non-default.
 */
class ViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                SignInViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AssetsViewModel::class.java) -> {
                AssetsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AssetDetailViewModel::class.java) -> {
                AssetDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(EditAssetViewModel::class.java) -> {
                EditAssetViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddAssetViewModel::class.java) -> {
                AddAssetViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoansViewModel::class.java) -> {
                LoansViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoanDetailViewModel::class.java) -> {
                LoanDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddLoanViewModel::class.java) -> {
                AddLoanViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ListUserdataViewModel::class.java) -> {
                ListUserdataViewModel(repository) as T
            }
            modelClass.isAssignableFrom(UserDataDetailViewModel::class.java) -> {
                UserDataDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) -> {
                ResetPasswordViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(
                    Injection.provideRepository(context)
                ).also { INSTANCE = it }
            }
        }
    }
}
