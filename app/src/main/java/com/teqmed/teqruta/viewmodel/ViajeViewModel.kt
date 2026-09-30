package com.teqmed.teqruta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teqmed.teqruta.model.Viaje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViajeViewModel : ViewModel() {

    private val _odometroInicio = MutableStateFlow("")
    val odometroInicio: StateFlow<String> = _odometroInicio

    private val _odometroFin = MutableStateFlow("")
    val odometroFin: StateFlow<String> = _odometroFin
    
    private val _pagoPeaje = MutableStateFlow(false)
    val pagoPeaje: StateFlow<Boolean> = _pagoPeaje
    
    private val _montoPeaje = MutableStateFlow("")
    val montoPeaje: StateFlow<String> = _montoPeaje

    fun onOdometroInicioChange(newValue: String) {
        _odometroInicio.value = newValue
    }

    fun onOdometroFinChange(newValue: String) {
        _odometroFin.value = newValue
    }

    fun onPagoPeajeChange(newValue: Boolean) {
        _pagoPeaje.value = newValue
    }

    fun onMontoPeajeChange(newValue: String) {
        _montoPeaje.value = newValue
    }

    fun finalizarViaje(clinicaId: Int) {
        viewModelScope.launch {
            val inicio = _odometroInicio.value.toIntOrNull() ?: 0
            val fin = _odometroFin.value.toIntOrNull() ?: 0
            val monto = _montoPeaje.value.toDoubleOrNull()
            
            val viaje = Viaje(
                clinicaId = clinicaId,
                odometroInicio = inicio,
                odometroFin = fin,
                pagoPeaje = _pagoPeaje.value,
                montoPeaje = monto,
                sincronizado = false // Offline first
            )
            // Aquí se llamaría a viajeDao.insertViaje(viaje)
            // y se programaría el WorkManager para sincronización.
        }
    }
}
