package com.teqmed.teqruta.data

data class UserProfile(
    val email: String,
    val nombre: String,
    val rol: String, // "tecnico", "jefe", "gerencia", "admin"
    val cargo: String,
    val vehiculoAsignado: String = "HK-8932 (Toyota Hilux)"
)

/**
 * Gestor de sesión global. Conserva los perfiles demo de la UI y, además,
 * almacena el JWT devuelto por el backend TeqRuta para las peticiones HTTP.
 */
object SessionManager {
    var currentUser: UserProfile = UserProfile(
        email = "hans.morales@teqmed.cl",
        nombre = "Hans Morales",
        rol = "tecnico",
        cargo = "Técnico de Servicio en Terreno",
        vehiculoAsignado = "HK-8932 (Toyota Hilux)"
    )

    /** JWT Bearer. Se lee desde el interceptor de OkHttp en cada petición. */
    var token: String? = null
        private set

    val vehiculoAsignado: String
        get() = currentUser.vehiculoAsignado

    var currentRole: String
        get() = currentUser.rol
        set(value) {
            currentUser = currentUser.copy(rol = value)
        }

    var currentUserEmail: String
        get() = currentUser.email
        set(value) {
            currentUser = currentUser.copy(email = value)
        }

    // Perfiles demostrativos para revisión docente y evaluación
    val DEMO_USERS = listOf(
        UserProfile(
            email = "tecnico@teqmed.cl",
            nombre = "Hans Morales (Técnico)",
            rol = "tecnico",
            cargo = "Técnico Especialista Terreno",
            vehiculoAsignado = "HK-8932 (Toyota Hilux 4x4)"
        ),
        UserProfile(
            email = "jefe@teqmed.cl",
            nombre = "Martina Norambuena (Jefatura)",
            rol = "jefe",
            cargo = "Jefa de Servicio Técnico Biobío",
            vehiculoAsignado = "Oficina Central Concepción"
        ),
        UserProfile(
            email = "gerencia@teqmed.cl",
            nombre = "Williams Contreras (Gerencia)",
            rol = "gerencia",
            cargo = "Gerente de Operaciones & Finanzas",
            vehiculoAsignado = "Dirección General"
        ),
        UserProfile(
            email = "admin@teqmed.cl",
            nombre = "Administrador del Sistema",
            rol = "admin",
            cargo = "Administrador de Flota & Plataforma",
            vehiculoAsignado = "Flota General"
        )
    )

    fun loginWithProfile(profile: UserProfile) {
        currentUser = profile
    }

    fun login(email: String, role: String, nombre: String = "") {
        val found = DEMO_USERS.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        currentUser = if (found != null) {
            found
        } else {
            val displayName = if (nombre.isNotBlank()) nombre else email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
            UserProfile(
                email = email.trim(),
                nombre = displayName,
                rol = role,
                cargo = when (role) {
                    "jefe" -> "Jefatura de Servicio Técnico"
                    "gerencia" -> "Control de Gestión & Finanzas"
                    "admin" -> "Administrador de Plataforma"
                    else -> "Técnico de Terreno"
                }
            )
        }
    }

    /** Guarda la sesión real devuelta por POST /api/auth/login. */
    fun saveBackendSession(
        role: String,
        token: String,
        email: String,
        nombre: String,
        cargo: String,
        vehiculoAsignado: String?
    ) {
        currentUser = UserProfile(
            email = email.trim(),
            nombre = nombre.ifBlank { email.substringBefore("@") },
            rol = role,
            cargo = cargo.ifBlank { "Usuario del sistema" },
            vehiculoAsignado = vehiculoAsignado?.takeIf { it.isNotBlank() }
                ?: "HK-8932 (Toyota Hilux)"
        )
        this.token = token
    }

    fun logout() {
        currentUser = UserProfile(
            email = "",
            nombre = "",
            rol = "",
            cargo = ""
        )
        token = null
    }

    fun isLoggedIn(): Boolean = currentUser.email.isNotBlank() && currentUser.rol.isNotBlank()
}
