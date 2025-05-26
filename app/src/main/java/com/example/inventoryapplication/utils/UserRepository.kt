package com.example.inventoryapplication.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inventoryapplication.data.api.ApiService
import com.example.inventoryapplication.data.api.User
import com.example.inventoryapplication.data.response.*
import com.example.inventoryapplication.models.*
import com.example.inventoryapplication.pref.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    private val _signUpResponse = MutableLiveData<Result<SignUpResponse>>()
    val signUpResponse: LiveData<Result<SignUpResponse>> = _signUpResponse

    private val _signInResponse = MutableLiveData<Result<SignInResponse>>()
    val signInResponse: LiveData<Result<SignInResponse>> = _signInResponse

    private val _homeResponse = MutableLiveData<Result<DashboardData>>()
    val homeResponse: LiveData<Result<DashboardData>> = _homeResponse

    private val _profileResponse = MutableLiveData<Result<UserData>>()
    val profileResponse: LiveData<Result<UserData>> = _profileResponse

    private val _updateProfileResponse = MutableLiveData<Result<UserDataUpdateProfile>>()
    val updateProfileResponse: LiveData<Result<UserDataUpdateProfile>> = _updateProfileResponse

    private val _assetsResponse = MutableLiveData<Result<AssetsResponse>>()
    val assetsResponse: LiveData<Result<AssetsResponse>> = _assetsResponse

    private val _assetDetailResponse = MutableLiveData<Result<AssetDetailResponse>>()
    val assetDetailResponse: LiveData<Result<AssetDetailResponse>> = _assetDetailResponse

    private val _editAssetResponse = MutableLiveData<Result<EditAssetResponse>>()
    val editAssetResponse: LiveData<Result<EditAssetResponse>> = _editAssetResponse

    private val _addAssetResponse = MutableLiveData<kotlin.Result<AddAssetResponse>> ()
    val addAssetResponse: LiveData<kotlin.Result<AddAssetResponse>> = _addAssetResponse

    private val _loansResponse = MutableLiveData<Result<LoansResponse>>()
    val loansResponse: LiveData<Result<LoansResponse>> = _loansResponse

    private val _loanDetailResponse = MutableLiveData<Result<LoanDetailResponse>>()
    val loanDetailResponse: LiveData<Result<LoanDetailResponse>> = _loanDetailResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun saveSession(user: UserModel) = userPreference.saveSession(user)
    fun getSession(): Flow<UserModel> = userPreference.getSession()
    suspend fun clearSession() = userPreference.signout()
    suspend fun saveSignInState() = userPreference.signin()

    // POST Sign Up
    suspend fun postSignUp(name: String, email: String, password: String) {
        _signUpResponse.postValue(Result.Loading)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.signup(name, email, password)
            }
            _signUpResponse.postValue(Result.Success(response))
            Log.d(TAG, "SignUp success: $response")
        } catch (e: Exception) {
            _signUpResponse.postValue(Result.Error(e))
            Log.e(TAG, "SignUp error", e)
        }
    }

    // POST Sign In
    suspend fun postSignIn(email: String, password: String): SignInResponse {
        clearSession() // wajib bersih dulu biar tidak reuse token lama

        val response = apiService.signin(email, password)
        val token = response.token ?: throw Exception("Token kosong")

        // Panggil profil setelah login
        val profile = apiService.getProfile("Bearer $token")
        val user = profile.user ?: throw Exception("User kosong")

        // Save session
        saveSession(
            UserModel(
                id = user.id,
                token = token,
                isSignIn = true,
                username = user.username,
                email = user.email,
                photoProfile = user.photoProfile,
                role = user.role ?: "unknown"
            )
        )

        return response
    }

    //GET Dashboard
    suspend fun getDashboardData(token: String, role: String): HomeResponse {
        val authHeader = "Bearer $token"
        _homeResponse.postValue(Result.Loading)
        return try {
            val response = when (role.lowercase()) {
                "admin" -> apiService.getAdminDashboard(authHeader)
                "user" -> apiService.getUserDashboard(authHeader)
                else -> throw IllegalArgumentException("Unknown role: $role")
            }
            _homeResponse.postValue(Result.Success(response.data))
            response
        } catch (e: Exception) {
            _homeResponse.postValue(Result.Error(e))
            throw e
        }
    }

    suspend fun refreshDashboard() {
        val session = getSession().first()
        if (session.token.isNotEmpty()) {
            getDashboardData(session.token, session.role ?: "user")
        }
    }

    //GET Assets
    suspend fun getAssets(token: String, role: String): List<Asset> {
        val response = when (role.lowercase()) {
            "admin" -> apiService.getAdminAssets("Bearer $token")
            "user" -> apiService.getUserAssets("Bearer $token")
            else -> throw IllegalArgumentException("Unknown role: $role")
        }

        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Failed to fetch assets: ${response.message()}")
        }
    }

    //GET Filtered Asset

    suspend fun getFilteredAssets(token: String, role: String, filter: String): List<Asset> {
        val response = when (role.lowercase()) {
            "admin" -> apiService.getFilteredAssets("Bearer $token", filter)
            "user" -> apiService.getUserFilteredAssets("Bearer $token", filter)
            else -> throw IllegalArgumentException("Unknown role: $role")
        }

        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Failed to fetch filtered assets: ${response.message()}")
        }
    }


    //GET Asset's Detail
    suspend fun getUserAssetDetail(token: String, id: Int): AssetDetailResponse {
        return apiService.getUserAssetDetail("Bearer $token", id)
    }

    suspend fun getAdminAssetDetail(token: String, id: Int): AssetDetailResponse {
        return apiService.getAdminAssetDetail("Bearer $token", id)
    }

    //PUT Edit Asset
    suspend fun editAsset(
        token: String,
        id: Int,
        assetEntry: String,
        placement: String,
        invStatus: String,
        isCanLoan: Boolean,
        expiredDate: String?,
        quantity: Int
    ): String {
        val body = mapOf(
            "asset_entry" to assetEntry,
            "placement" to placement,
            "inv_status" to invStatus,
            "is_can_loan" to isCanLoan.toString(),
            "expired_date" to expiredDate,
            "quantity" to quantity
        )

        return try {
            val response = apiService.editAsset("Bearer $token", id, body)
            response.message
        } catch (e: Exception) {
            throw Exception("Edit asset gagal: ${e.message}")
        }
    }

    //DEL Hapus Aset
    suspend fun deleteAsset(token: String, assetId: Int): Response<DeleteAssetResponse> {
        return apiService.deleteAsset("Bearer $token", assetId)
    }

    //POST Add Asset
    private fun String.toPlainRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    suspend fun postAddAssetWithPhoto(
        token: String,
        name: String,
        category: String,
        placement: String,
        assetEntry: String,
        expiredDate: String,
        isCanLoan: Boolean,
        invStatus: String,
        quantity: Int,
        imageFile: File?
    ): Asset {
        val namePart = name.toPlainRequestBody()
        val categoryPart = category.toPlainRequestBody()
        val placementPart = placement.toPlainRequestBody()
        val assetEntryPart = assetEntry.toPlainRequestBody()
        val expiredDatePart = expiredDate.toPlainRequestBody()
        val isCanLoanPart = (if (isCanLoan) "true" else "false")
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val invStatusPart = invStatus.toPlainRequestBody()
        val quantityPart = quantity.toString().toPlainRequestBody()

        val photoPart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("img_url", it.name, requestFile) // <-- Updated key name
        }

        val response = apiService.addAsset(
            token = "Bearer $token",
            name = namePart,
            category = categoryPart,
            placement = placementPart,
            assetEntry = assetEntryPart,
            expiredDate = expiredDatePart,
            isCanLoan = isCanLoanPart,
            invStatus = invStatusPart,
            quantity = quantityPart,
            photo = photoPart
        )

        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Empty response body")
        } else {
            throw Exception("Add asset failed: ${response.message()}")
        }
    }

    //GET Loans
    suspend fun getLoans(token: String, role: String): List<Loan> {
        val response = when (role.lowercase()) {
            "admin" -> apiService.getAdminLoans("Bearer $token")
            "user" -> apiService.getUserLoans("Bearer $token")
            else -> throw IllegalArgumentException("Unknown role: $role")
        }

        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Failed to fetch loans: ${response.message()}")
        }
    }

    //GET Loan's Detail
    suspend fun getUserLoanDetail(token: String, id: Int): LoanDetailResponse {
        val response = apiService.getUserLoanDetail(token = "Bearer $token", id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty loan detail response")
        } else {
            throw Exception("Failed to get user loan detail: ${response.message()}")
        }
    }

    suspend fun getAdminLoanDetail(token: String, id: Int): LoanDetailResponse {
        val response = apiService.getAdminLoanDetail(token = "Bearer $token", id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty loan detail response")
        } else {
            throw Exception("Failed to get user loan detail: ${response.message()}")
        }
    }

    //DEL Hapus Loan
    suspend fun deleteLoan(token: String, loanId: Int): Response<DeleteLoanResponse> {
        return apiService.deleteLoan("Bearer $token", loanId)
    }

    //POST Add Loan
    suspend fun postAddLoan(token: String, request: AddLoanRequest): AddLoanResponse {
        val response = apiService.addLoan("Bearer $token", request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty add loan response")
        } else {
            throw Exception("Failed to add loan: ${response.message()}")
        }
    }

    //GET Profile
    suspend fun getProfile(token: String) {
        _profileResponse.postValue(Result.Loading)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getProfile("Bearer $token")
            }
            response.user?.let {
                _profileResponse.postValue(Result.Success(it))
                Log.d(TAG, "Profile fetched: $it")
            } ?: run {
                _profileResponse.postValue(Result.Error(Exception("Empty user data")))
                Log.e(TAG, "Profile error: Empty user data")
            }
        } catch (e: Exception) {
            _profileResponse.postValue(Result.Error(e))
            Log.e(TAG, "Profile request failed", e)
        }
    }

    //POST Edit Profile
    suspend fun updateProfile(token: String, username: String, email: String, photoFile: File?): Boolean {
        _isLoading.postValue(true)
        return withContext(Dispatchers.IO) {
            val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = photoFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photo_profile", it.name, requestFile)
            }
            try {
                val response = apiService.updateProfile("Bearer $token", usernamePart, emailPart, photoPart)
                response.user?.let {
                    _updateProfileResponse.postValue(Result.Success(it))
                    Log.d(TAG, "Profile updated: $it")
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Update profile error", e)
                false
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    //Change Password
    suspend fun changePassword(
        token: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        _isLoading.postValue(true)
        return withContext(Dispatchers.IO) {
            val currentPassPart = currentPassword.toRequestBody("text/plain".toMediaTypeOrNull())
            val newPassPart = newPassword.toRequestBody("text/plain".toMediaTypeOrNull())
            val confirmPassPart = confirmPassword.toRequestBody("text/plain".toMediaTypeOrNull())

            try {
                val response = apiService.changePassword("Bearer $token", currentPassPart, newPassPart, confirmPassPart)
                response.user?.let {
                    Log.d(TAG, "Password updated: $it")
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Change password error", e)
                false
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    //GET All User
    suspend fun getAllUsers(token: String): List<User> {
        Log.e("USER TOKEN", token)
        return apiService.getAllUsers("Bearer $token").data
    }

    //GET User's Detail
    suspend fun getUserById(token: String, userId: Int): User {
        val response = apiService.getUserDetailById("Bearer $token", userId)
        if (response.isSuccessful) {
            return response.body()?.user ?: throw Exception("User kosong")
        } else {
            throw Exception("Gagal ambil detail user: ${response.message()}")
        }
    }

    //PUT Reset Password User
    suspend fun resetUserPassword(token: String, userId: Int, newPassword: String): User {
        return try {
            val response = apiService.resetPassword("Bearer $token", userId, newPassword, newPassword)
            if (response.isSuccessful) {
                response.body()?.user ?: throw Exception("Reset password gagal: data user kosong")
            } else {
                throw Exception("Reset password gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Reset password error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "UserRepository"

        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(pref: UserPreference, service: ApiService): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(pref, service)
            }.also { instance = it }
    }
}
