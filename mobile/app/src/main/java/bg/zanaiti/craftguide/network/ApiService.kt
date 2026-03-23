package bg.zanaiti.craftguide.network

import bg.zanaiti.craftguide.models.AnswerCheckRequest
import bg.zanaiti.craftguide.models.AnswerCheckResponse
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.models.QuizQuestion
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/crafts")
    suspend fun getAllCrafts(): List<Craft>

    @GET("api/crafts/{id}/language/{lang}")
    suspend fun getCraftByIdAndLanguage(
        @Path("id") id: Long,
        @Path("lang") lang: String
    ): Craft

    @GET("api/crafts/nearby")
    suspend fun getCraftsNearby(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double
    ): List<Craft>

    // Въпроси за занаят
    @GET("api/quiz/craft/{craftId}")
    suspend fun getQuestionsForCraft(
        @Path("craftId") craftId: Long,
        @Query("lang") lang: String = "bg"
    ): List<QuizQuestion>

    // Проверка на отговор
    @POST("api/quiz/{questionId}/check")
    suspend fun checkAnswer(
        @Path("questionId") questionId: Long,
        @Query("selectedOption") selectedOption: Int
    ): AnswerCheckResponse

    // Запазване на точки (само за логнати)
    @POST("api/progress/user/{userId}/craft/{craftId}/complete-quiz")
    suspend fun completeQuiz(
        @Path("userId") userId: Long,
        @Path("craftId") craftId: Long,
        @Query("correctAnswersCount") correctAnswersCount: Int,
        @Query("lang") lang: String = "bg"
    ): retrofit2.Response<Unit>
}