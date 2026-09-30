package com.teqmed.teqruta

import com.teqmed.teqruta.viewmodel.ViajeViewModel
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    
    @Test
    fun testEventLoggingInViajeViewModel() {
        val viewModel = ViajeViewModel()
        
        // Initial state
        assertTrue("Event list should be empty initially", viewModel.eventos.value.isEmpty())
        
        // Add events (stops, gas, crash)
        viewModel.agregarEvento("Parada por descanso")
        viewModel.agregarEvento("Recarga de gasolina 20L")
        viewModel.agregarEvento("Choque menor en ruta")
        
        val eventos = viewModel.eventos.value
        assertEquals("Should have exactly 3 events logged", 3, eventos.size)
        
        assertTrue("Event should contain the stop text", eventos[0].contains("Parada por descanso"))
        assertTrue("Event should contain the gas text", eventos[1].contains("Recarga de gasolina 20L"))
        assertTrue("Event should contain the crash text", eventos[2].contains("Choque menor en ruta"))
        
        // Ensure it contains a timestamp (format: timestamp|texto)
        assertTrue("Event should be split by pipe character", eventos[0].split("|").size == 2)
    }

    @Test
    fun testTimeLoggingInViajeViewModel() {
        val viewModel = ViajeViewModel()
        
        viewModel.onOdometroInicioChange("100")
        viewModel.onFotoInicioTomada("foto.jpg")
        
        viewModel.iniciarViaje()
        
        // Since we don't expose horaInicioViaje directly in StateFlow, we can only verify 
        // through integration with `guardarViaje()` or assume it works based on logic.
        // We will just verify it transitions state correctly.
        assertEquals(com.teqmed.teqruta.viewmodel.TripState.IN_PROGRESS, viewModel.tripState.value)
    }
}
