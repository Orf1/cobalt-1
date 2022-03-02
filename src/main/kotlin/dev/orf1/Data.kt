package dev.orf1

@kotlinx.serialization.Serializable
internal data class LoginRequest(val email: String, val password: String)

@kotlinx.serialization.Serializable
internal data class RegisterRequest(val email: String, val password: String, val username: String)

@kotlinx.serialization.Serializable
internal data class AuthResponse(val username: String, val token: String)

@kotlinx.serialization.Serializable
internal data class Profile(val email: String, val password: String, val username: String, val token: String)

@kotlinx.serialization.Serializable
internal data class EncryptedProfile(
    val email: String,
    val hashedPassword: String,
    val username: String,
    var token: String
)