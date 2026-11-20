package com.teqmed.teqruta.data.repository

import com.teqmed.teqruta.data.SessionManager
import com.teqmed.teqruta.data.network.ApiErrorParser
import com.teqmed.teqruta.data.network.AuthService
import com.teqmed.teqruta.data.network.dto.LoginRequestWire

/** Login real contra POST /api/auth/login; guarda el JWT y el perfil en SessionManager. */
class RetrofitAuthRepository(
    private val service: AuthService
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResult {
        if (email.isBlank() || password.isBlank()) {
            return LoginResult.Error("Ingrese su correo y contraseña.")
        }

        return try {
            val response = service.login(LoginRequestWire(email.trim(), password))
            if (!response.isSuccessful) {
                SessionManager.logout()
                LoginResult.Error(ApiErrorParser.message(response))
            } else {
                val data = response.body()?.data
                if (data == null || data.accessToken.isBlank() || data.user.rol.isBlank()) {
                    SessionManager.logout()
                    LoginResult.Error("Respuesta de autenticación inválida del servidor.")
                } else {
                    SessionManager.saveBackendSession(
                        role = data.user.rol,
                        token = data.accessToken,
                        email = data.user.email,
                        nombre = data.user.nombre.orEmpty(),
                        cargo = data.user.cargo.orEmpty(),
                        vehiculoAsignado = data.user.vehiculoAsignado
                    )
                    LoginResult.Success(data.user.rol)
                }
            }
        } catch (e: Exception) {
            SessionManager.logout()
            LoginResult.Error(ApiErrorParser.message(e))
        }
    }
}
