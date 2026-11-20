package com.teqmed.teqruta.data.repository

/** Contrato de autenticación. Hoy se resuelve localmente; mañana contra el backend. */
interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
}

sealed interface LoginResult {
    data class Success(val role: String) : LoginResult
    data class Error(val message: String) : LoginResult
}

/**
 * Mantiene el comportamiento actual de la app (usuarios de prueba) sin tocar la UI.
 * Cuando llegue la API se reemplaza por una implementación que consuma `/api/auth/login`
 * y guarde el JWT devuelto.
 */
class StubAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        val role = when (email.trim().lowercase()) {
            "tecnico@teqruta.cl" -> "tecnico"
            "jefe@teqruta.cl" -> "jefe"
            "gerencia@teqruta.cl" -> "gerencia"
            "admin@teqruta.cl" -> "admin"
            else -> null
        }

        return if (role != null) {
            LoginResult.Success(role)
        } else {
            LoginResult.Error("Credenciales no válidas. Verifique el correo electrónico ingresado.")
        }
    }
}
