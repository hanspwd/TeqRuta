package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.data.network.dto.DestinoDto
import com.teqmed.teqruta.data.network.dto.TarifaCombustibleDto
import com.teqmed.teqruta.data.network.dto.ViajeDto

/**
 * Contrato de comunicación con la API TeqRuta.
 *
 * La interfaz de dominio no tiene anotaciones HTTP: [RetrofitTeqRutaApi] es la
 * implementación real y [StubTeqRutaApi] se conserva como doble de pruebas.
 */
interface TeqRutaApi {
    suspend fun obtenerDestinos(): Result<List<DestinoDto>>

    suspend fun obtenerTarifaCombustible(): Result<TarifaCombustibleDto>

    /** Debe ser idempotente por uuid en el servidor. */
    suspend fun subirViajes(viajes: List<ViajeDto>): Result<List<String>>
}
