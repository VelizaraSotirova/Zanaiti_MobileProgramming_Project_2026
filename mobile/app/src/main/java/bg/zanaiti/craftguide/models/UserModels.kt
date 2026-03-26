package bg.zanaiti.craftguide.models

data class UserProfile(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val totalPoints: Int,
    val createdAt: String
)

data class UserStats(
    val totalPoints: Int,
    val craftsVisited: Int,
    val quizzesCompleted: Int,
    val averageScore: Double
)

data class UserProgressDto(
    val id: Long?,
    val userId: Long,
    val craftId: Long,
    val craftName: String?,
    val pointsEarned: Int,
    val quizCompleted: Boolean,
    val attemptCount: Int,
    val lastInteractionDate: String?,
    val quizCompletionDate: String?,
    val quizScore: Int?,
    val languageCode: String?
)

data class PointsHistoryItem(
    val id: Long,
    val points: Int,
    val source: String,
    val description: String,
    val createdAt: String,
    val craftName: String?
)