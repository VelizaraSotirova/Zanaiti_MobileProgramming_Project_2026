package bg.zanaiti.craftguide.models

data class QuizQuestion(
    val id: Long,
    val craftId: Long,
    val translations: Map<String, QuizQuestionTranslation>,
    val correctOptionIndex: Int,
    val pointsReward: Int,
    val active: Boolean
)

data class QuizQuestionTranslation(
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String
)

data class AnswerCheckRequest(
    val selectedOption: Int
)

data class AnswerCheckResponse(
    val correct: Boolean,
    val message: String
)