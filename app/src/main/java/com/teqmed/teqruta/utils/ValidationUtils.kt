package com.teqmed.teqruta.utils

/**
 * Lógica de validación desacoplada y centralizada (Cumple con IE 2.2.1).
 * Todas las reglas de negocio y restricciones de entrada se procesan aquí.
 */
object ValidationUtils {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "El correo no puede estar vacío")
        }
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (!trimmed.matches(emailRegex)) {
            return ValidationResult(false, "Ingrese un correo válido (ej. usuario@teqmed.cl)")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(false, "La contraseña no puede estar vacía")
        }
        if (password.length < 4) {
            return ValidationResult(false, "La contraseña debe tener al menos 4 caracteres")
        }
        return ValidationResult(true)
    }

    fun validateOdometroInicio(valor: String): ValidationResult {
        val trimmed = valor.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Ingrese el odómetro inicial")
        }
        val num = trimmed.toIntOrNull()
        if (num == null) {
            return ValidationResult(false, "El odómetro debe ser un número entero")
        }
        if (num <= 0) {
            return ValidationResult(false, "El odómetro inicial debe ser mayor a 0 km")
        }
        if (num > 999999) {
            return ValidationResult(false, "Valor de odómetro fuera de rango válido (< 1.000.000 km)")
        }
        return ValidationResult(true)
    }

    fun validateOdometroFin(valor: String, inicio: Int?): ValidationResult {
        val trimmed = valor.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Ingrese el odómetro final")
        }
        val num = trimmed.toIntOrNull()
        if (num == null) {
            return ValidationResult(false, "El odómetro debe ser un número entero")
        }
        if (num <= 0) {
            return ValidationResult(false, "El odómetro final debe ser mayor a 0 km")
        }
        if (inicio != null && num <= inicio) {
            return ValidationResult(
                false,
                "El odómetro final ($num km) debe ser mayor al inicial ($inicio km)"
            )
        }
        if (inicio != null && (num - inicio) > 2000) {
            return ValidationResult(
                false,
                "La distancia recorrida (+${num - inicio} km) excede el máximo razonable por viaje"
            )
        }
        return ValidationResult(true)
    }

    fun validateMontoPeaje(valor: String): ValidationResult {
        val trimmed = valor.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Ingrese el monto del peaje")
        }
        val num = trimmed.toDoubleOrNull()
        if (num == null) {
            return ValidationResult(false, "El monto debe ser un número válido")
        }
        if (num <= 0) {
            return ValidationResult(false, "El monto debe ser superior a $0 CLP")
        }
        if (num > 100000) {
            return ValidationResult(false, "El monto de peaje supera el límite institucional ($100.000)")
        }
        return ValidationResult(true)
    }

    fun validateEvento(valor: String): ValidationResult {
        val trimmed = valor.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Describa la observación o evento")
        }
        if (trimmed.length < 3) {
            return ValidationResult(false, "El detalle debe tener al menos 3 caracteres")
        }
        return ValidationResult(true)
    }

    fun validateRuta(ruta: String): ValidationResult {
        if (ruta.trim().isEmpty()) {
            return ValidationResult(false, "Seleccione o ingrese un destino médico")
        }
        return ValidationResult(true)
    }

    fun validateVehiculo(vehiculo: String): ValidationResult {
        if (vehiculo.trim().isEmpty()) {
            return ValidationResult(false, "Seleccione un vehículo de la flota")
        }
        return ValidationResult(true)
    }

    fun validateObservacionJefe(observacion: String): ValidationResult {
        val trimmed = observacion.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Debe ingresar el motivo de la observación")
        }
        if (trimmed.length < 5) {
            return ValidationResult(false, "La observación debe detallar la discrepancia (mínimo 5 caracteres)")
        }
        return ValidationResult(true)
    }
}
