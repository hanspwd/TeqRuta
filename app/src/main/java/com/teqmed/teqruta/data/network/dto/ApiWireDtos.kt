package com.teqmed.teqruta.data.network.dto

import com.google.gson.annotations.SerializedName

/** Envoltura idéntica a la de la API TeqRuta: {"data": ..., "message": ..., "meta": ...}. */
data class ApiEnvelope<T>(
    val data: T?,
    val message: String? = null,
    val meta: Any? = null
)

data class ErrorEnvelope(val error: ErrorBodyWire?)
data class ErrorBodyWire(val code: String?, val message: String?)

// ---------------- Auth ----------------

data class LoginRequestWire(val email: String, val password: String)

data class LoginDataWire(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val user: UserWire
)

data class UserWire(
    val email: String,
    val nombre: String? = null,
    val rol: String,
    val cargo: String? = null,
    @SerializedName("vehiculoAsignado") val vehiculoAsignado: String? = null,
    val activo: Boolean? = null
)

// ---------------- Mantenedores ----------------

data class DestinoWire(
    val id: Long,
    val nombre: String,
    val distanciaOficialKm: Double,
    val region: String? = null,
    val comuna: String? = null,
    val activo: Boolean? = null
)

data class TarifaWire(
    val id: Long? = null,
    val combustible: String? = null,
    val precioLitro: Double,
    val moneda: String? = null,
    val origen: String? = null,
    val fuente: String? = null,
    val vigenteDesde: String? = null,
    val activa: Boolean? = null
)

// ---------------- Sync de viajes ----------------

data class SyncRequestWire(val viajes: List<ViajeDto>)

data class SyncResponseWire(
    val resumen: SyncResumenWire? = null,
    val rechazados: List<SyncRechazoWire>? = null,
    val viajes: List<SyncViajeWire>? = null
)

data class SyncResumenWire(
    val recibidos: Int? = null,
    val insertados: Int? = null,
    val duplicados: Int? = null,
    val rechazados: Int? = null
)

data class SyncRechazoWire(val indice: Int? = null, val errores: List<String>? = null)

data class SyncViajeWire(val uuid: String? = null, val estado: String? = null)
