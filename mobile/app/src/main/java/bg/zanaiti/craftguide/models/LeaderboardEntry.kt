package bg.zanaiti.craftguide.models

data class LeaderboardEntry(
    val userId: Long,
    val username: String,
    val totalPoints: Int
)