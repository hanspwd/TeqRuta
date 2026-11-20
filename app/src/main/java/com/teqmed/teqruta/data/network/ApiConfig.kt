package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.BuildConfig

/** Configuración de red del backend Spring Boot. */
object ApiConfig {
    /** BaseUrl de Retrofit siempre termina en '/'; se normaliza si viene sin ella. */
    val baseUrl: String = BuildConfig.API_BASE_URL.trim().let {
        if (it.endsWith("/")) it else "$it/"
    }

    /** Patente usada cuando el usuario autenticado no tiene una asignada (datos demo del seed). */
    const val VEHICULO_PATENTE_DEFAULT = "HK-8932"

    /**
     * Extrae la patente de textos como "HK-8932 (Toyota Hilux 4x4)".
     * El backend valida formato de patente, no la descripción del vehículo.
     */
    fun normalizarPatente(valor: String?): String {
        val patron = Regex("""\b[A-Z]{2}\s*-?\s*\d{4}\b""", RegexOption.IGNORE_CASE)
        val limpio = patron.find(valor.orEmpty())?.value
            ?.replace(Regex("""[^A-Z0-9]"""), "")
            ?.uppercase()
            .orEmpty()
        return if (limpio.length == 6) {
            "${limpio.substring(0, 2)}-${limpio.substring(2)}"
        } else {
            VEHICULO_PATENTE_DEFAULT
        }
    }
}
