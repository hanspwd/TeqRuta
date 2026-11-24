package com.teqmed.teqruta.data.network

import com.teqmed.teqruta.data.network.dto.ApiEnvelope
import com.teqmed.teqruta.data.network.dto.DestinoDto
import com.teqmed.teqruta.data.network.dto.DestinoWire
import com.teqmed.teqruta.data.network.dto.SyncRequestWire
import com.teqmed.teqruta.data.network.dto.SyncResponseWire
import com.teqmed.teqruta.data.network.dto.TarifaCombustibleDto
import com.teqmed.teqruta.data.network.dto.TarifaWire
import com.teqmed.teqruta.data.network.dto.ViajeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Servicio HTTP interno. El resto de la app sigue dependiendo solo de TeqRutaApi. */
internal interface TeqRutaService {
    @GET("api/mantenedores/destinos")
    suspend fun obtenerDestinos(): Response<ApiEnvelope<List<DestinoWire>>>

    @GET("api/mantenedores/tarifas/vigente")
    suspend fun obtenerTarifa(): Response<ApiEnvelope<TarifaWire>>

    @POST("api/viajes/sync")
    suspend fun sincronizar(@Body body: SyncRequestWire): Response<ApiEnvelope<SyncResponseWire>>
}

/** Implementación real del contrato contra la API TeqRuta. */
class RetrofitTeqRutaApi internal constructor(
    private val service: TeqRutaService
) : TeqRutaApi {

    override suspend fun obtenerDestinos(): Result<List<DestinoDto>> = try {
        val response = service.obtenerDestinos()
        if (!response.isSuccessful) {
            Result.failure(IllegalStateException(ApiErrorParser.message(response)))
        } else {
            Result.success(
                (response.body()?.data ?: emptyList()).map {
                    DestinoDto(
                        id = it.id.toString(),
                        nombre = it.nombre,
                        distanciaOficialKm = it.distanciaOficialKm
                    )
                }
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun obtenerTarifaCombustible(): Result<TarifaCombustibleDto> = try {
        val response = service.obtenerTarifa()
        if (!response.isSuccessful) {
            Result.failure(IllegalStateException(ApiErrorParser.message(response)))
        } else {
            val tarifa = response.body()?.data
            if (tarifa == null) {
                Result.failure(IllegalStateException("El servidor no devolvió la tarifa vigente"))
            } else {
                Result.success(
                    TarifaCombustibleDto(
                        precioPorLitro = tarifa.precioLitro,
                        fecha = tarifa.vigenteDesde.orEmpty(),
                        fuente = tarifa.fuente?.takeIf { it.isNotBlank() }
                            ?: tarifa.origen?.takeIf { it.isNotBlank() }
                            ?: "API_EXTERNA"
                    )
                )
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun subirViajes(viajes: List<ViajeDto>): Result<List<String>> {
        if (viajes.isEmpty()) return Result.success(emptyList())

        return try {
            val response = service.sincronizar(SyncRequestWire(viajes))
            if (!response.isSuccessful) {
                Result.failure(IllegalStateException(ApiErrorParser.message(response)))
            } else {
                val data = response.body()?.data
                val aceptados = data?.viajes?.mapNotNull { it.uuid }.orEmpty()
                if (aceptados.isEmpty() && !data?.rechazados.isNullOrEmpty()) {
                    val detalle = data?.rechazados?.firstOrNull()?.errores?.joinToString(", ")
                    Result.failure(
                        IllegalStateException(detalle ?: "El servidor rechazó todos los viajes")
                    )
                } else {
                    // Incluye insertados y duplicados (idempotencia); nunca incluye rechazados.
                    Result.success(aceptados)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
