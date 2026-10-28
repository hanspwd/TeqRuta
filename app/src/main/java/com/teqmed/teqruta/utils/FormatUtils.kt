package com.teqmed.teqruta.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades de formateo para texto legible para humanos y formatos de Chile (Cumple requerimiento de UI legible)
 */
object FormatUtils {

    private val spanishLocale = Locale.forLanguageTag("es-CL")
    private val currencySymbols = DecimalFormatSymbols(spanishLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun formatCurrency(amount: Double): String {
        val formatter = DecimalFormat("$#,##0", currencySymbols)
        return formatter.format(amount)
    }

    fun formatNumber(number: Int): String {
        val formatter = DecimalFormat("#,##0", currencySymbols)
        return formatter.format(number)
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Fecha no registrada"
        val sdf = SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm 'hrs'", spanishLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("dd/MM/yyyy", spanishLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatGps(lat: Double?, lng: Double?): String {
        if (lat == null || lng == null) return "Ubicación no disponible"
        return "GPS: Lat ${"%.4f".format(lat)}, Lng ${"%.4f".format(lng)}"
    }
}
