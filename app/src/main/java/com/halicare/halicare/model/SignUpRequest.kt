package com.halicare.halicare.model
data class SignUpRequest(
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val password: String,
    val confirmPassword: String,
    val user_type: String = "PATIENT"
)
