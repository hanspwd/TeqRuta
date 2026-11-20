package com.teqmed.teqruta.data.repository

import com.teqmed.teqruta.data.SessionManager
import com.teqmed.teqruta.data.network.ApiConfig
import com.teqmed.teqruta.data.network.TeqRutaApi
import com.teqmed.teqruta.data.network.dto.DestinoDto
import com.teqmed.teqruta.data.network.dto.TarifaCombustibleDto
import com.teqmed.teqruta.data.network.dto.toDto
import com.teqmed.teqruta.model.Viaje
import com.teqmed.teqruta.model.ViajeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Único punto de acceso a los datos de viajes.
 *
 * Room sigue siendo la fuente de verdad en terreno. La API se usa para sincronizar
 * cuando hay red y sesión iniciada; los UUID aceptados por el backend se marcan como
 * sincronizados. Los rechazados quedan pendientes para el siguiente intento.
 */
class ViajeRepository(
    private val viajeDao: ViajeDao,
    private val api: TeqRutaApi
) {
    fun observarViajes(): Flow<List<Viaje>> = viajeDao.getAllViajes()

    suspend fun guardarViaje(viaje: Viaje) = withContext(Dispatchers.IO) {
        viajeDao.insertViaje(viaje)
    }

    suspend fun sincronizarPendientes(): Int {
        // Sin JWT el backend respondería 401; no gastamos el lote.
        if (SessionManager.token.isNullOrBlank()) return 0

        val pendientes = withContext(Dispatchers.IO) { viajeDao.getViajesPendientes() }
        if (pendientes.isEmpty()) return 0

        val tecnicoEmailSesion = SessionManager.currentUserEmail.ifBlank { "tecnico@teqmed.cl" }
        val patenteSesion = ApiConfig.normalizarPatente(SessionManager.vehiculoAsignado)

        val resultado = api.subirViajes(
            pendientes.map { viaje ->
                viaje.toDto(
                    tecnicoEmail = viaje.tecnicoEmail.ifBlank { tecnicoEmailSesion },
                    vehiculoPatente = ApiConfig.normalizarPatente(
                        viaje.vehiculoPatente.ifBlank { patenteSesion }
                    )
                )
            }
        )

        return resultado.fold(
            onSuccess = { uuidsAceptados ->
                if (uuidsAceptados.isEmpty()) {
                    0
                } else {
                    withContext(Dispatchers.IO) {
                        viajeDao.marcarComoSincronizados(uuidsAceptados)
                    }
                    uuidsAceptados.size
                }
            },
            onFailure = { 0 }
        )
    }

    suspend fun obtenerDestinos(): Result<List<DestinoDto>> = api.obtenerDestinos()

    suspend fun obtenerTarifaCombustible(): Result<TarifaCombustibleDto> =
        api.obtenerTarifaCombustible()
}
