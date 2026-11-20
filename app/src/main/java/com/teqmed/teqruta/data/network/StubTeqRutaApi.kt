package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.data.network.dto.DestinoDto
import com.teqmed.teqruta.data.network.dto.TarifaCombustibleDto
import com.teqmed.teqruta.data.network.dto.ViajeDto

/**
 * Implementación temporal de [TeqRutaApi]. No hace llamadas de red.
 *
 * `subirViajes` devuelve una lista vacía a propósito: así los viajes siguen
 * marcados como "en espera de sincronización" hasta que se conecte la API real.
 */
class StubTeqRutaApi : TeqRutaApi {

    override suspend fun obtenerDestinos(): Result<List<DestinoDto>> = Result.success(
        listOf(
            DestinoDto("dst-001", "Santiago - Rancagua", 87.0),
            DestinoDto("dst-002", "Chillán - Los Ángeles", 115.0),
            DestinoDto("dst-003", "Valparaíso - Viña del Mar", 35.0),
            DestinoDto("dst-004", "Concepción - Talcahuano", 25.0)
        )
    )

    override suspend fun obtenerTarifaCombustible(): Result<TarifaCombustibleDto> =
        Result.success(
            TarifaCombustibleDto(
                precioPorLitro = 0.0,
                fecha = "sin fecha",
                fuente = "STUB local - pendiente de conectar API externa"
            )
        )

    override suspend fun subirViajes(viajes: List<ViajeDto>): Result<List<String>> =
        Result.success(emptyList())
}
