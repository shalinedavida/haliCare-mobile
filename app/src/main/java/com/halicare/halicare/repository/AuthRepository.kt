package com.halicare.halicare.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.model.LoginRequest
import com.halicare.halicare.model.LoginResponse
import com.halicare.halicare.model.SignUpRequest


class AuthRepository(private val api: ApiInterface,private val tokenProvider: AuthTokenProvider) {

    suspend fun login(phone: String, password: String): Result<LoginResponse> {
        return try {
            val response: Response<LoginResponse> = withContext(Dispatchers.IO) {
                api.loginUser(LoginRequest(phone, password))
            }
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Login: Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Login API Error: Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception(parseLoginError(errorBody, response.code())))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login Network/Exception: ${e.message}", e)
            Result.failure(Exception("Login Network error: ${e.message ?: e.javaClass.simpleName}"))
        }
    }


    suspend fun register(signUpRequest: SignUpRequest): Result<Unit> {
        return try {
            val response: Response<Unit> = withContext(Dispatchers.IO) {
                api.registerUser(signUpRequest)
            }
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Register API Error: Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception(parseRegisterError(errorBody, response.code())))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register Network/Exception: ${e.message}", e)
            Result.failure(Exception("Register Network error: ${e.message ?: e.javaClass.simpleName}"))
        }
    }


    private fun parseLoginError(errorBody: String?, statusCode: Int): String {
        if (errorBody == null) return "Invalid phone number or password."
        if (errorBody.contains("Invalid credentials", ignoreCase = true) ||
            errorBody.contains("No active account found", ignoreCase = true)) {
            return "Invalid phone number or password."
        }
        if (errorBody.contains("\"detail\":")) {
            return errorBody.substringAfter("\"detail\":\"").substringBefore("\"")
        }
        return "Invalid phone number or password."
    }


    private fun parseRegisterError(errorBody: String?, statusCode: Int): String {
        Log.d("AuthRepository", "Parsing Register Error: $errorBody, Code: $statusCode")
        if (errorBody == null) return "Registration failed (Code: $statusCode). Please try again."
        if (errorBody.contains("already exists", ignoreCase = true)) {
            return "An account with this phone number already exists."
        }
        if (errorBody.contains("\"detail\":")) {
            return errorBody.substringAfter("\"detail\":\"").substringBefore("\"")
        }
        return "Registration failed: $errorBody"
    }
}

