package com.halicare.halicare.model
import com.google.gson.annotations.SerializedName
data class LoginResponse(
    val token: String,
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("user_type")
    val userType: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("phone_number")
    val phoneNumber: String
)