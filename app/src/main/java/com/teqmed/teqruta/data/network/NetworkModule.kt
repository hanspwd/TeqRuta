package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.BuildConfig
import com.teqmed.teqruta.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP único de la app. El token se lee en cada petición desde SessionManager,
 * así funciona aunque el login ocurra después de crear el cliente.
 */
object NetworkModule {

    init {
        require(
            ApiConfig.baseUrl.startsWith("http://") || ApiConfig.baseUrl.startsWith("https://")
        ) { "teqrutaApiBaseUrl debe empezar con http:// o https://" }
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            // Necesario con Ngrok free; inofensivo contra un servidor local.
            .header("Ngrok-Skip-Browser-Warning", "1")

        SessionManager.token?.takeIf { it.isNotBlank() }?.let { token ->
            request.header("Authorization", "Bearer $token")
        }
        chain.proceed(request.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createApi(): TeqRutaApi =
        RetrofitTeqRutaApi(retrofit.create(TeqRutaService::class.java))

    fun createAuthService(): AuthService = retrofit.create(AuthService::class.java)
}
