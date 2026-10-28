package com.teqmed.teqruta.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teqmed.teqruta.data.AppDatabase
import com.teqmed.teqruta.model.Viaje
import com.teqmed.teqruta.utils.LocationHelper
import com.teqmed.teqruta.utils.ValidationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class TripState {
    SETUP, IN_PROGRESS, FINISHED
}

data class PeajeInfo(
    val id: String = UUID.randomUUID().toString(),
    val monto: Double,
    val bitmap: Bitmap? = null
)

class ViajeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val viajeDao = db.viajeDao()

    // Status of trip
    private val _tripState = MutableStateFlow(TripState.SETUP)
    val tripState: StateFlow<TripState> = _tripState

    private val _rutaSeleccionada = MutableStateFlow("Santiago - Rancagua (Clínica RedSalud)")
    val rutaSeleccionada: StateFlow<String> = _rutaSeleccionada

    // Vehicle
    private val _vehiculoPatente = MutableStateFlow("HK-8932 (Toyota Hilux)")
    val vehiculoPatente: StateFlow<String> = _vehiculoPatente

    // Form: Inicio
    private val _odometroInicio = MutableStateFlow("")
    val odometroInicio: StateFlow<String> = _odometroInicio
    
    private val _odometroInicioError = MutableStateFlow<String?>(null)
    val odometroInicioError: StateFlow<String?> = _odometroInicioError
    
    private val _fotoInicioBitmap = MutableStateFlow<Bitmap?>(null)
    val fotoInicioBitmap: StateFlow<Bitmap?> = _fotoInicioBitmap

    private val _locationInicio = MutableStateFlow<LocationHelper.LocationCoordinates?>(null)
    val locationInicio: StateFlow<LocationHelper.LocationCoordinates?> = _locationInicio

    // Form: Peajes & Eventos
    private val _peajes = MutableStateFlow<List<PeajeInfo>>(emptyList())
    val peajes: StateFlow<List<PeajeInfo>> = _peajes
    
    private val _montoPeajeInput = MutableStateFlow("")
    val montoPeajeInput: StateFlow<String> = _montoPeajeInput
    
    private val _montoPeajeError = MutableStateFlow<String?>(null)
    val montoPeajeError: StateFlow<String?> = _montoPeajeError

    private val _fotoPeajeActual = MutableStateFlow<Bitmap?>(null)
    val fotoPeajeActual: StateFlow<Bitmap?> = _fotoPeajeActual

    private val _eventos = MutableStateFlow<List<String>>(emptyList())
    val eventos: StateFlow<List<String>> = _eventos
    
    private val _eventoInput = MutableStateFlow("")
    val eventoInput: StateFlow<String> = _eventoInput

    // Form: Fin
    private val _odometroFin = MutableStateFlow("")
    val odometroFin: StateFlow<String> = _odometroFin
    
    private val _odometroFinError = MutableStateFlow<String?>(null)
    val odometroFinError: StateFlow<String?> = _odometroFinError

    private val _fotoFinBitmap = MutableStateFlow<Bitmap?>(null)
    val fotoFinBitmap: StateFlow<Bitmap?> = _fotoFinBitmap

    private val _locationFin = MutableStateFlow<LocationHelper.LocationCoordinates?>(null)
    val locationFin: StateFlow<LocationHelper.LocationCoordinates?> = _locationFin

    private var horaInicioViaje: Long = 0L

    // Room Database Flow of viajes
    val viajesDb: StateFlow<List<Viaje>> = viajeDao.getAllViajes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-populate seed data if database is empty
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            viajeDao.getAllViajes().firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val sample1 = Viaje(
            rutaSeleccionada = "Chillán - Los Ángeles (Hospital Clínico)",
            odometroInicio = 10500,
            fotoOdometroInicio = "sample1_start",
            odometroFin = 10620,
            fotoOdometroFin = "sample1_end",
            totalMontoPeajes = 4500.0,
            fotosPeajes = "peaje1.jpg",
            horaInicio = System.currentTimeMillis() - 86400000,
            horaFin = System.currentTimeMillis() - 80000000,
            eventosExtra = "Carga combustible 20L",
            latitudInicio = -36.6067,
            longitudInicio = -72.1034,
            latitudFin = -37.4697,
            longitudFin = -72.3537,
            tecnicoEmail = "tecnico@teqruta.cl",
            vehiculoPatente = "HK-8932",
            sincronizado = true,
            estadoAprobacion = "APROBADO"
        )
        val sample2 = Viaje(
            rutaSeleccionada = "Santiago - Rancagua (Clínica El Salud)",
            odometroInicio = 15200,
            fotoOdometroInicio = "sample2_start",
            odometroFin = 15290,
            fotoOdometroFin = "sample2_end",
            totalMontoPeajes = 2900.0,
            fotosPeajes = "peaje2.jpg",
            horaInicio = System.currentTimeMillis() - 43200000,
            horaFin = System.currentTimeMillis() - 36000000,
            eventosExtra = "Tráfico pesado en peaje Angostura",
            latitudInicio = -33.4489,
            longitudInicio = -70.6693,
            latitudFin = -34.1708,
            longitudFin = -70.7444,
            tecnicoEmail = "tecnico@teqruta.cl",
            vehiculoPatente = "HK-8932",
            sincronizado = false,
            estadoAprobacion = "PENDIENTE"
        )
        viajeDao.insertViaje(sample1)
        viajeDao.insertViaje(sample2)
    }

    // Input handlers with real-time validation (IE 2.2.1)
    fun onRutaChange(ruta: String) { _rutaSeleccionada.value = ruta }
    fun onVehiculoChange(vehiculo: String) { _vehiculoPatente.value = vehiculo }

    fun onOdometroInicioChange(valor: String) {
        _odometroInicio.value = valor
        val res = ValidationUtils.validateOdometroInicio(valor)
        _odometroInicioError.value = res.errorMessage
    }

    fun onFotoInicioTomada(bitmap: Bitmap) {
        _fotoInicioBitmap.value = bitmap
    }

    fun onOdometroFinChange(valor: String) {
        _odometroFin.value = valor
        val startVal = _odometroInicio.value.toIntOrNull()
        val res = ValidationUtils.validateOdometroFin(valor, startVal)
        _odometroFinError.value = res.errorMessage
    }

    fun onFotoFinTomada(bitmap: Bitmap) {
        _fotoFinBitmap.value = bitmap
    }

    fun onMontoPeajeChange(valor: String) {
        _montoPeajeInput.value = valor
        val res = ValidationUtils.validateMontoPeaje(valor)
        _montoPeajeError.value = res.errorMessage
    }

    fun onFotoPeajeTomada(bitmap: Bitmap) {
        _fotoPeajeActual.value = bitmap
    }

    fun onEventoInputChange(valor: String) {
        _eventoInput.value = valor
    }

    fun agregarPeaje() {
        val valResult = ValidationUtils.validateMontoPeaje(_montoPeajeInput.value)
        if (!valResult.isValid) {
            _montoPeajeError.value = valResult.errorMessage
            return
        }
        val monto = _montoPeajeInput.value.toDoubleOrNull() ?: return
        val list = _peajes.value.toMutableList()
        list.add(PeajeInfo(monto = monto, bitmap = _fotoPeajeActual.value))
        _peajes.value = list
        
        // Reset peaje fields
        _montoPeajeInput.value = ""
        _montoPeajeError.value = null
        _fotoPeajeActual.value = null
    }

    fun eliminarPeaje(peajeId: String) {
        _peajes.value = _peajes.value.filter { it.id != peajeId }
    }

    fun agregarEvento() {
        val valResult = ValidationUtils.validateEvento(_eventoInput.value)
        if (valResult.isValid) {
            val list = _eventos.value.toMutableList()
            list.add("${System.currentTimeMillis()}|${_eventoInput.value.trim()}")
            _eventos.value = list
            _eventoInput.value = ""
        }
    }

    fun iniciarViaje(context: Context) {
        val valResult = ValidationUtils.validateOdometroInicio(_odometroInicio.value)
        if (!valResult.isValid || _fotoInicioBitmap.value == null) {
            _odometroInicioError.value = valResult.errorMessage ?: "Debe tomar foto del odómetro inicial"
            return
        }
        
        // Native Location GPS Access (IE 2.4.1)
        val coords = LocationHelper.getCurrentLocation(context)
        _locationInicio.value = coords

        horaInicioViaje = System.currentTimeMillis()
        _tripState.value = TripState.IN_PROGRESS
    }

    fun terminarViaje(context: Context) {
        val startVal = _odometroInicio.value.toIntOrNull()
        val valResult = ValidationUtils.validateOdometroFin(_odometroFin.value, startVal)
        if (!valResult.isValid || _fotoFinBitmap.value == null) {
            _odometroFinError.value = valResult.errorMessage ?: "Debe tomar foto del odómetro final"
            return
        }

        // Native Location GPS Access (IE 2.4.1)
        val coords = LocationHelper.getCurrentLocation(context)
        _locationFin.value = coords

        _tripState.value = TripState.FINISHED
        guardarViajeEnRoom()
    }

    fun reset() {
        _tripState.value = TripState.SETUP
        _odometroInicio.value = ""
        _odometroInicioError.value = null
        _fotoInicioBitmap.value = null
        _locationInicio.value = null
        
        _odometroFin.value = ""
        _odometroFinError.value = null
        _fotoFinBitmap.value = null
        _locationFin.value = null

        _peajes.value = emptyList()
        _montoPeajeInput.value = ""
        _montoPeajeError.value = null
        _fotoPeajeActual.value = null

        _eventos.value = emptyList()
        _eventoInput.value = ""
        horaInicioViaje = 0L
    }

    private fun guardarViajeEnRoom() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val inicio = _odometroInicio.value.toIntOrNull() ?: 0
            val fin = _odometroFin.value.toIntOrNull() ?: 0
            val totalPeajes = _peajes.value.sumOf { it.monto }
            val eventosStr = _eventos.value.joinToString(";")
            
            val viaje = Viaje(
                rutaSeleccionada = _rutaSeleccionada.value,
                odometroInicio = inicio,
                fotoOdometroInicio = "cap_start_${System.currentTimeMillis()}",
                odometroFin = fin,
                fotoOdometroFin = "cap_end_${System.currentTimeMillis()}",
                totalMontoPeajes = totalPeajes,
                fotosPeajes = "${_peajes.value.size} comprobante(s)",
                horaInicio = if (horaInicioViaje > 0) horaInicioViaje else System.currentTimeMillis(),
                horaFin = System.currentTimeMillis(),
                eventosExtra = eventosStr,
                latitudInicio = _locationInicio.value?.latitude,
                longitudInicio = _locationInicio.value?.longitude,
                latitudFin = _locationFin.value?.latitude,
                longitudFin = _locationFin.value?.longitude,
                tecnicoEmail = "tecnico@teqruta.cl",
                vehiculoPatente = _vehiculoPatente.value,
                sincronizado = false,
                estadoAprobacion = "PENDIENTE"
            )

            viajeDao.insertViaje(viaje)
        }
    }

    // Actions for Jefe / Admin dashboards
    fun cambiarEstadoAprobacion(uuid: String, nuevoEstado: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            viajeDao.actualizarEstadoAprobacion(uuid, nuevoEstado)
        }
    }

    fun sincronizarPendientes() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pendientes = viajeDao.getViajesPendientes()
            if (pendientes.isNotEmpty()) {
                val uuids = pendientes.map { it.uuid }
                viajeDao.marcarComoSincronizados(uuids)
            }
        }
    }
}
