package bg.zanaiti.craftguide.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String
)

data class AuthResponse(
    val token: String,
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val totalPoints: Int,
    val role: String
)