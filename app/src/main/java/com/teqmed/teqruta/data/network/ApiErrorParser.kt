package com.teqmed.teqruta.data.network

import com.google.gson.Gson
import com.teqmed.teqruta.data.network.dto.ErrorEnvelope
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Convierte errores HTTP/red en mensajes legibles para la UI. */
internal object ApiErrorParser {

    private val gson = Gson()

    fun message(throwable: Throwable): String = when (throwable) {
        is IOException ->
            "No se pudo conectar con el servidor. Revise la URL base y su conexión a red."
        is HttpException -> message(throwable.response())
        else -> throwable.message?.takeIf { it.isNotBlank() } ?: "Error inesperado"
    }

    fun message(response: Response<*>?): String {
        if (response == null) return "Error inesperado del servidor"
        val raw = runCatching { response.errorBody()?.string() }.getOrNull().orEmpty()
        val backendMessage = runCatching {
            gson.fromJson(raw, ErrorEnvelope::class.java)?.error?.message
        }.getOrNull()

        return backendMessage?.takeIf { it.isNotBlank() }
            ?: if (response.code() == 401) {
                "Sesión expirada o no autorizada. Inicie sesión nuevamente."
            } else {
                "Error del servidor (HTTP ${response.code()})"
            }
    }
}
