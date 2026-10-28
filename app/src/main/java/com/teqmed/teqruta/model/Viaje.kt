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
    val fotosPeajes: String, // URIs or representation separated by comma
    val horaInicio: Long = System.currentTimeMillis(),
    val horaFin: Long = System.currentTimeMillis(),
    val eventosExtra: String = "",
    val latitudInicio: Double? = null,
    val longitudInicio: Double? = null,
    val latitudFin: Double? = null,
    val longitudFin: Double? = null,
    val tecnicoEmail: String = "hans.morales@teqmed.cl",
    val vehiculoPatente: String = "HK-8932",
    var sincronizado: Boolean = false,
    var estadoAprobacion: String = "PENDIENTE", // PENDIENTE, APROBADO, OBSERVADO
    val observacionJefe: String = "",
    val costoCombustibleEstimado: Double = 0.0
)
