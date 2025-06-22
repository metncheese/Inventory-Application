package com.example.inventoryapplication.data.api

import com.example.inventoryapplication.data.response.AddAssetResponse
import com.example.inventoryapplication.data.response.AddLoanResponse
import com.example.inventoryapplication.data.response.AssetDetailResponse
import com.example.inventoryapplication.data.response.AssetsResponse
import com.example.inventoryapplication.data.response.DeleteAssetResponse
import com.example.inventoryapplication.data.response.DeleteLoanResponse
import com.example.inventoryapplication.data.response.EditAssetResponse
import com.example.inventoryapplication.data.response.HomeResponse
import com.example.inventoryapplication.data.response.ListUserDataResponse
import com.example.inventoryapplication.data.response.LoanDetailResponse
import com.example.inventoryapplication.data.response.LoansResponse
import com.example.inventoryapplication.data.response.ProfileResponse
import com.example.inventoryapplication.data.response.SignInResponse
import com.example.inventoryapplication.data.response.SignUpResponse
import com.example.inventoryapplication.data.response.UpdateProfileResponse
import com.example.inventoryapplication.data.response.UserDetailResponse
import com.example.inventoryapplication.utils.AddLoanRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    //Sign Up
    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("signup")
    suspend fun signup(
        @Field("username") fullName: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("confirmation_password") confirmPassword: String
    ): SignUpResponse

    //Sign In
    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("signin")
    suspend fun signin(
        @Field("email") email: String,
        @Field("password") password: String
    ): SignInResponse

    //Profile Fragment
    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    //Edit Profile
    @Multipart
    @Headers("Accept: application/json")
    @POST("profile/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part photo: MultipartBody.Part?
    ): UpdateProfileResponse

    //Change Password
    @Multipart
    @Headers("Accept: application/json")
    @POST("profile/update")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Part("current_password") currentPassword: RequestBody,
        @Part("new_password") newPassword: RequestBody,
        @Part("new_password_confirmation") newPasswordConfirmation: RequestBody
    ): UpdateProfileResponse

    //Home Fragment
    @GET("admin/dashboard")
    suspend fun getAdminDashboard(
        @Header("Authorization") token: String
    ): HomeResponse

    @GET("user/dashboard")
    suspend fun getUserDashboard(
        @Header("Authorization") token: String
    ): HomeResponse

    //Asset Fragment
    @GET("admin/assets")
    suspend fun getAdminAssets(
        @Header("Authorization") token: String
    ): Response<AssetsResponse>

    @GET("user/assets")
    suspend fun getUserAssets(
        @Header("Authorization") token: String
    ): Response<AssetsResponse>

    //Filter Asset
    @GET("admin/assets")
    suspend fun getFilteredAssets(
        @Header("Authorization") token: String,
        @Query("filter") filter: String
    ): Response<AssetsResponse>

    @GET("user/assets")
    suspend fun getUserFilteredAssets(
        @Header("Authorization") token: String,
        @Query("filter") filter: String
    ): Response<AssetsResponse>

    //Detail Asset
    @GET("user/assets/{id}")
    suspend fun getUserAssetDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): AssetDetailResponse

    @GET("admin/assets/{id}")
    suspend fun getAdminAssetDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): AssetDetailResponse

    //Edit Asset
    @Headers("Accept: application/json")
    @PUT("admin/assets/{id}")
    suspend fun editAsset(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): EditAssetResponse

    //Delete Asset
    @DELETE("admin/assets/{id}")
    @Headers("Accept: application/json")
    suspend fun deleteAsset(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DeleteAssetResponse>

    //Add Asset
    @Multipart
    @Headers("Accept: application/json")
    @POST("admin/assets")
    suspend fun addAsset(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("placement") placement: RequestBody,
        @Part("asset_entry") assetEntry: RequestBody,
        @Part("expired_date") expiredDate: RequestBody,
        @Part("is_can_loan") isCanLoan: RequestBody,
        @Part("inv_status") invStatus: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<AddAssetResponse>

    //Loan Fragment
    @GET("admin/loans")
    suspend fun getAdminLoans(
        @Header("Authorization") token: String
    ): Response<LoansResponse>

    @GET("user/loans")
    suspend fun getUserLoans(
        @Header("Authorization") token: String
    ): Response<LoansResponse>

    //Detail Loan
    @GET("admin/loans/{id}")
    suspend fun getAdminLoanDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LoanDetailResponse>

    @GET("user/loans/{id}")
    suspend fun getUserLoanDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LoanDetailResponse>

    //Delete Loan
    @DELETE("admin/loans/{id}")
    @Headers("Accept: application/json")
    suspend fun deleteLoan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DeleteLoanResponse>


    //Add Loan
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("admin/loans")
    suspend fun addLoan(
        @Header("Authorization") token: String,
        @Body request: AddLoanRequest
    ): Response<AddLoanResponse>

    //List User Data
    @GET("admin/users") // Ganti sesuai endpoint kamu
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): ListUserDataResponse

    //GET User Detail
    @GET("admin/users/{id}")
    suspend fun getUserDetailById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<UserDetailResponse>

    //PUT Reset Password User
    @FormUrlEncoded
    @PUT("admin/users/{id}")
    suspend fun resetPassword(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Field("password") newPassword: String,
        @Field("password_confirmation") confirmPassword: String
    ): Response<UserDetailResponse>
}
