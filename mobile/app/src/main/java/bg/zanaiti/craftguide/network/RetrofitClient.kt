package bg.zanaiti.craftguide.network

import android.annotation.SuppressLint
import bg.zanaiti.craftguide.utils.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @SuppressLint("StaticFieldLeak")
    private lateinit var tokenManager: TokenManager

    // Инициализация – извиква се в MainActivity
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    // Интерцептор за JWT токен
    private val authInterceptor = Interceptor { chain ->
        val token = if (::tokenManager.isInitialized) tokenManager.getToken() else null

        android.util.Log.d("AuthInterceptor", "Token is ${if (token != null) "present (${token.take(20)}...)" else "NULL"}")


        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            // автоматично добавя хедър Authorization: Bearer <token> към заявката
            requestBuilder.header("Authorization", "Bearer $token")
            android.util.Log.d("AuthInterceptor", "Added Authorization header")
        } else {
            android.util.Log.d("AuthInterceptor", "No token - request will be unauthorized")
        }
        chain.proceed(requestBuilder.build())
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS // HEADERS - за дебъг на токени
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.2:8080/")   // ← 10.0.2.2 = localhost за емулатора / IPv4 = телефон
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}