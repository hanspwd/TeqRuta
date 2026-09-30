package com.teqmed.teqruta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teqmed.teqruta.model.Viaje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class TripState {
    SETUP, IN_PROGRESS, FINISHED
}

data class PeajeInfo(val monto: Double, val fotoUri: String)

class ViajeViewModel : ViewModel() {

    private val _tripState = MutableStateFlow(TripState.SETUP)
    val tripState: StateFlow<TripState> = _tripState

    private val _rutaSeleccionada = MutableStateFlow("Santiago - Rancagua")
    val rutaSeleccionada: StateFlow<String> = _rutaSeleccionada

    // Inicio
    private val _odometroInicio = MutableStateFlow("")
    val odometroInicio: StateFlow<String> = _odometroInicio
    
    private val _fotoInicio = MutableStateFlow<String?>(null)
    val fotoInicio: StateFlow<String?> = _fotoInicio

    // Peajes
    private val _peajes = MutableStateFlow<List<PeajeInfo>>(emptyList())
    val peajes: StateFlow<List<PeajeInfo>> = _peajes
    
    // Fin
    private val _odometroFin = MutableStateFlow("")
    val odometroFin: StateFlow<String> = _odometroFin
    
    private val _fotoFin = MutableStateFlow<String?>(null)
    val fotoFin: StateFlow<String?> = _fotoFin

    // Logging
    private val _eventos = MutableStateFlow<List<String>>(emptyList())
    val eventos: StateFlow<List<String>> = _eventos
    
    private var horaInicioViaje: Long = 0L

    // Historial simulado para la vista
    private val _historial = MutableStateFlow<List<Viaje>>(
        listOf(
            Viaje(rutaSeleccionada = "Chillán - Los Ángeles", odometroInicio = 1000, fotoOdometroInicio = "img1", odometroFin = 1200, fotoOdometroFin = "img2", totalMontoPeajes = 5000.0, fotosPeajes = "", sincronizado = true),
            Viaje(rutaSeleccionada = "Santiago - Rancagua", odometroInicio = 1500, fotoOdometroInicio = "img3", odometroFin = 1590, fotoOdometroFin = "img4", totalMontoPeajes = 2500.0, fotosPeajes = "", sincronizado = false)
        )
    )
    val historial: StateFlow<List<Viaje>> = _historial

    fun onRutaChange(ruta: String) { _rutaSeleccionada.value = ruta }
    fun onOdometroInicioChange(valor: String) { _odometroInicio.value = valor }
    fun onFotoInicioTomada(uri: String) { _fotoInicio.value = uri }
    
    fun onOdometroFinChange(valor: String) { _odometroFin.value = valor }
    fun onFotoFinTomada(uri: String) { _fotoFin.value = uri }
    
    fun agregarPeaje(monto: Double, uri: String) {
        val list = _peajes.value.toMutableList()
        list.add(PeajeInfo(monto, uri))
        _peajes.value = list
    }
    
    fun agregarEvento(evento: String) {
        val list = _eventos.value.toMutableList()
        list.add("${System.currentTimeMillis()}|$evento")
        _eventos.value = list
    }

    fun iniciarViaje() {
        if (_odometroInicio.value.isNotEmpty() && _fotoInicio.value != null) {
            horaInicioViaje = System.currentTimeMillis()
            _tripState.value = TripState.IN_PROGRESS
        }
    }

    fun terminarViaje() {
        if (_odometroFin.value.isNotEmpty() && _fotoFin.value != null) {
            _tripState.value = TripState.FINISHED
            guardarViaje()
        }
    }
    
    fun reset() {
        _tripState.value = TripState.SETUP
        _odometroInicio.value = ""
        _fotoInicio.value = null
        _odometroFin.value = ""
        _fotoFin.value = null
        _peajes.value = emptyList()
        _eventos.value = emptyList()
        horaInicioViaje = 0L
    }

    private fun guardarViaje() {
        viewModelScope.launch {
            val inicio = _odometroInicio.value.toIntOrNull() ?: 0
            val fin = _odometroFin.value.toIntOrNull() ?: 0
            val totalPeajes = _peajes.value.sumOf { it.monto }
            val fotosPeajes = _peajes.value.joinToString(",") { it.fotoUri }
            val eventosJson = _eventos.value.joinToString(";") // Simple representation
            
            val viaje = Viaje(
                rutaSeleccionada = _rutaSeleccionada.value,
                odometroInicio = inicio,
                fotoOdometroInicio = _fotoInicio.value,
                odometroFin = fin,
                fotoOdometroFin = _fotoFin.value,
                totalMontoPeajes = totalPeajes,
                fotosPeajes = fotosPeajes,
                horaInicio = horaInicioViaje,
                horaFin = System.currentTimeMillis(),
                eventosExtra = eventosJson,
                sincronizado = false
            )
            
            val currentList = _historial.value.toMutableList()
            currentList.add(0, viaje)
            _historial.value = currentList
            // viajeDao.insertViaje(viaje)
        }
    }
}
