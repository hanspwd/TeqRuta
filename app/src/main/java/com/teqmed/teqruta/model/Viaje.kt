package com.teqmed.teqruta.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "viajes")
data class Viaje(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val rutaSeleccionada: String,
    val odometroInicio: Int,
    val fotoOdometroInicio: String?,
    val odometroFin: Int,
    val fotoOdometroFin: String?,
    val totalMontoPeajes: Double,
    val fotosPeajes: String, // IDs or URIs separated by comma
    val horaInicio: Long = 0L,
    val horaFin: Long = 0L,
    val eventosExtra: String = "",
    var sincronizado: Boolean = false
)
