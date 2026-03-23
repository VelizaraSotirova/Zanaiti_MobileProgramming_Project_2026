package bg.zanaiti.craftguide.models

data class ApiResponse<T>(
    val data: T?,
    val message: String?,
    val status: Int
)