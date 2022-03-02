package dev.orf1

@kotlinx.serialization.Serializable
data class LoginRequest(val email: String, val password: String)
@kotlinx.serialization.Serializable
data class RegisterRequest(val email: String, val password: String, val username: String)
@kotlinx.serialization.Serializable
data class AuthenticationResponse(val username: String, val token: String)