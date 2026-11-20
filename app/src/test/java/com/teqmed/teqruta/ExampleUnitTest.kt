package com.teqmed.teqruta

import com.teqmed.teqruta.data.network.ApiConfig
import com.teqmed.teqruta.utils.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas puras de reglas de negocio y configuración (sin Android framework). */
class ExampleUnitTest {

    @Test
    fun validaCorreoDelBackend() {
        assertTrue(ValidationUtils.validateEmail("tecnico@teqmed.cl").isValid)
        assertFalse(ValidationUtils.validateEmail("correo-invalido").isValid)
    }

    @Test
    fun validaLongitudDeContrasena() {
        assertFalse(ValidationUtils.validatePassword("abc").isValid)
        assertTrue(ValidationUtils.validatePassword("tecnico123").isValid)
    }

    @Test
    fun validaOdometroInicio() {
        assertFalse(ValidationUtils.validateOdometroInicio("0").isValid)
        assertTrue(ValidationUtils.validateOdometroInicio("1000").isValid)
    }

    @Test
    fun validaOdometroFinMayorAlInicial() {
        assertFalse(ValidationUtils.validateOdometroFin("900", 1000).isValid)
        assertTrue(ValidationUtils.validateOdometroFin("1023", 1000).isValid)
    }

    @Test
    fun normalizaPatenteDesdeDescripcionDelVehiculo() {
        assertEquals("HK-8932", ApiConfig.normalizarPatente("HK-8932 (Toyota Hilux 4x4)"))
        assertEquals("HK-8932", ApiConfig.normalizarPatente("hk 8932"))
    }
}
