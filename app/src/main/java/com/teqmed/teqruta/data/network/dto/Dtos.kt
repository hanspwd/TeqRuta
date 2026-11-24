package com.teqmed.teqruta.data.network.dto

import com.teqmed.teqruta.model.Viaje

/**
 * DTOs del contrato HTTP. La API TeqRuta exige tecnicoEmail y vehiculoPatente
 * en cada viaje del lote de sincronización.
 */

data class DestinoDto(
    val id: String,
    val nombre: String,
    val distanciaOficialKm: Double
)

data class TarifaCombustibleDto(
    val precioPorLitro: Double,
    val fecha: String,
    val fuente: String
)

data class ViajeDto(
    val uuid: String,
    val rutaSeleccionada: String,
    val odometroInicio: Int,
    val odometroFin: Int,
    val totalMontoPeajes: Double,
    val horaInicio: Long,
    val horaFin: Long,
    val eventosExtra: String,
    val sincronizado: Boolean,
    val tecnicoEmail: String,
    val vehiculoPatente: String,
    val fotoOdometroInicio: String? = null,
    val fotoOdometroFin: String? = null,
    val fotosPeajes: String = ""
)

/** Las fotos se envían como texto (URI/local path) según el contrato actual del backend. */
fun Viaje.toDto(tecnicoEmail: String, vehiculoPatente: String): ViajeDto = ViajeDto(
    uuid = uuid,
    rutaSeleccionada = rutaSeleccionada,
    odometroInicio = odometroInicio,
    odometroFin = odometroFin,
    totalMontoPeajes = totalMontoPeajes,
    horaInicio = horaInicio,
    horaFin = horaFin,
    eventosExtra = eventosExtra,
    sincronizado = sincronizado,
    tecnicoEmail = tecnicoEmail,
    vehiculoPatente = vehiculoPatente,
    fotoOdometroInicio = fotoOdometroInicio,
    fotoOdometroFin = fotoOdometroFin,
    fotosPeajes = fotosPeajes
)
