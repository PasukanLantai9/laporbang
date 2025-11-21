package com.example.laporbang.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laporbang.data.model.AuthResult
import com.example.laporbang.data.model.User
import com.example.laporbang.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val resetPasswordState: StateFlow<AuthResult> = _resetPasswordState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val forgotPasswordState: StateFlow<AuthResult> = _forgotPasswordState.asStateFlow()

    private val _otpState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val otpState: StateFlow<AuthResult> = _otpState.asStateFlow()
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading

            // Validasi input
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthResult.Error("Email and password cannot be empty")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthResult.Error("Invalid email format")
                return@launch
            }

            val result = repository.login(email, password)

            _authState.value = if (result.isSuccess) {
                AuthResult.Success(result.getOrNull()!!)
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading

            // Validasi input
            if (email.isBlank() || phoneNumber.isBlank() ||
                password.isBlank() || confirmPassword.isBlank()) {
                _authState.value = AuthResult.Error("All fields are required")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthResult.Error("Invalid email format")
                return@launch
            }

            if (password != confirmPassword) {
                _authState.value = AuthResult.Error("Passwords do not match")
                return@launch
            }

            if (password.length < 6) {
                _authState.value = AuthResult.Error("Password must be at least 6 characters")
                return@launch
            }

            val result = repository.register(email, password, phoneNumber)

            _authState.value = if (result.isSuccess) {
                AuthResult.Success(result.getOrNull()!!)
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = AuthResult.Loading

            if (email.isBlank()) {
                _forgotPasswordState.value = AuthResult.Error("Email cannot be empty")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _forgotPasswordState.value = AuthResult.Error("Invalid email format")
                return@launch
            }

            val result = repository.sendPasswordResetEmail(email)

            _forgotPasswordState.value = if (result.isSuccess) {
                AuthResult.Success(User(email = email))
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }

    fun verifyOTP(code: String) {
        viewModelScope.launch {
            _otpState.value = AuthResult.Loading

            if (code.isBlank() || code.length != 6) {
                _otpState.value = AuthResult.Error("Invalid OTP code")
                return@launch
            }

            val result = repository.verifyOTP(code)

            _otpState.value = if (result.isSuccess) {
                AuthResult.Success(User())
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "OTP verification failed")
            }
        }
    }

    fun confirmPasswordReset(code: String, newPass: String, confirmPass: String) {
        viewModelScope.launch {
            _resetPasswordState.value = AuthResult.Loading

            if (newPass.isBlank() || confirmPass.isBlank()) {
                _resetPasswordState.value = AuthResult.Error("Password cannot be empty")
                return@launch
            }

            if (newPass != confirmPass) {
                _resetPasswordState.value = AuthResult.Error("Passwords do not match")
                return@launch
            }

            if (newPass.length < 6) {
                _resetPasswordState.value = AuthResult.Error("Password too short")
                return@launch
            }

            val result = repository.confirmPasswordReset(code, newPass)

            _resetPasswordState.value = if (result.isSuccess) {
                AuthResult.Success(User()) // User dummy, yang penting sukses
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Failed to reset password")
            }
        }
    }


    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = repository.signInWithGoogle(idToken)

            _authState.value = if (result.isSuccess) {
                AuthResult.Success(result.getOrNull()!!)
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Google Sign In Failed")
            }
        }
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = AuthResult.Idle
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthResult.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthResult.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = AuthResult.Idle
    }

    fun resetOTPState() {
        _otpState.value = AuthResult.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
}