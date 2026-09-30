package com.teqmed.teqruta.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ViajeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViaje(viaje: Viaje)

    @Query("SELECT * FROM viajes WHERE sincronizado = 0")
    suspend fun getViajesPendientes(): List<Viaje>

    @Query("UPDATE viajes SET sincronizado = 1 WHERE uuid IN (:uuids)")
    suspend fun marcarComoSincronizados(uuids: List<String>)
    
    @Query("SELECT * FROM viajes")
    fun getAllViajes(): Flow<List<Viaje>>
}
