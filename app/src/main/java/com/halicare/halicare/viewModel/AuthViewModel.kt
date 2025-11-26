package com.halicare.halicare.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.model.LoginResponse
import com.halicare.halicare.model.SignUpRequest
import com.halicare.halicare.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenProvider: AuthTokenProvider
) : ViewModel() {


    sealed class LoginUiState {
        object Idle : LoginUiState()
        object Loading : LoginUiState()
        data class Success(val response: LoginResponse) : LoginUiState()
        data class Error(val errorMessage: String) : LoginUiState()
    }


    sealed class SignUpUiState {
        object Idle : SignUpUiState()
        object Loading : SignUpUiState()
        data class Success(val message: String = "User registered successfully") : SignUpUiState()
        data class Error(val errorMessage: String) : SignUpUiState()
    }


    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState


    private val _signUpState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val signUpState: StateFlow<SignUpUiState> = _signUpState


    fun loginUser(phone_number: String, password: String) = viewModelScope.launch {
        _loginState.value = LoginUiState.Loading
        val result = repository.login(phone_number, password)
        if (result.isSuccess) {
            val loginResponse = result.getOrNull()
            if (loginResponse != null) {
                tokenProvider.token = loginResponse.token
                _loginState.value = LoginUiState.Success(loginResponse)
            } else {
                _loginState.value = LoginUiState.Error("Unexpected empty response")
            }
        } else {
            val error = result.exceptionOrNull()
            _loginState.value = LoginUiState.Error("Network error: ${error?.localizedMessage ?: "Unknown error"}")
        }
    }


    fun registerUser(
        first_name: String,
        last_name: String,
        phone_number: String,
        password: String,
        confirmPassword: String
    ) = viewModelScope.launch {
        _signUpState.value = SignUpUiState.Loading
        val signUpRequest = SignUpRequest(first_name, last_name, phone_number, password, confirmPassword)
        val result = repository.register(signUpRequest)
        if (result.isSuccess) {
            _signUpState.value = SignUpUiState.Success()
        } else {
            val error = result.exceptionOrNull()
            _signUpState.value = SignUpUiState.Error("Registration failed: ${error?.localizedMessage ?: result.toString()}")
        }
    }
}

