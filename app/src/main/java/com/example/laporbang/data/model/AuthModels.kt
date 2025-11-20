package com.example.laporbang.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val phoneNumber: String,
    val password: String,
    val confirmPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class OTPVerificationRequest(
    val code: String
)