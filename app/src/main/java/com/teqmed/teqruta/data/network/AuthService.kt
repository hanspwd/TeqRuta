package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.data.network.dto.ApiEnvelope
import com.teqmed.teqruta.data.network.dto.LoginDataWire
import com.teqmed.teqruta.data.network.dto.LoginRequestWire
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Servicio de autenticación de la API TeqRuta. */
interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestWire): Response<ApiEnvelope<LoginDataWire>>
}
