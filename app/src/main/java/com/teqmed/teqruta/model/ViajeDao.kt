package com.teqmed.teqruta.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ViajeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertViaje(viaje: Viaje)

    @Query("SELECT * FROM viajes WHERE sincronizado = 0")
    fun getViajesPendientes(): List<Viaje>

    @Query("UPDATE viajes SET sincronizado = 1 WHERE uuid IN (:uuids)")
    fun marcarComoSincronizados(uuids: List<String>)

    @Query("UPDATE viajes SET estadoAprobacion = :estado WHERE uuid = :uuid")
    fun actualizarEstadoAprobacion(uuid: String, estado: String)

    @Query("UPDATE viajes SET estadoAprobacion = :estado, observacionJefe = :observacion WHERE uuid = :uuid")
    fun actualizarEstadoAprobacionConObservacion(uuid: String, estado: String, observacion: String)

    @Query("SELECT * FROM viajes ORDER BY horaInicio DESC")
    fun getAllViajes(): Flow<List<Viaje>>

    @Query("DELETE FROM viajes WHERE uuid = :uuid")
    fun deleteViajeByUuid(uuid: String)
}
