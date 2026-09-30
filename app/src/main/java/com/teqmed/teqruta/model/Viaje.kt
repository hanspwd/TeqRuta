package com.teqmed.teqruta.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "viajes")
data class Viaje(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val clinicaId: Int,
    val odometroInicio: Int,
    val odometroFin: Int,
    val pagoPeaje: Boolean,
    val montoPeaje: Double?,
    var sincronizado: Boolean = false
)
