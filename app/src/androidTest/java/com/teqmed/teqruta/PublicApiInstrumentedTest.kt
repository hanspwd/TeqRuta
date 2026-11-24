package com.teqmed.teqruta

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.teqmed.teqruta.data.SessionManager
import com.teqmed.teqruta.data.network.ApiConfig
import com.teqmed.teqruta.data.network.NetworkModule
import com.teqmed.teqruta.data.network.dto.LoginRequestWire
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verificación E2E en un dispositivo/emulador contra la URL pública que Gradle
 * embebió en BuildConfig (normalmente el túnel Ngrok de TeqRuta-API).
 *
 * Se omite por defecto para no depender de la red en CI. Ejecutarla con:
 *
 *   adb shell am instrument -w -e teqrutaLive 1 \
 *     com.teqmed.teqruta.test/androidx.test.runner.AndroidJUnitRunner
 *
 * o con Gradle, pasando el argumento al runner.
 */
@RunWith(AndroidJUnit4::class)
class PublicApiInstrumentedTest {

    private val argumentos = InstrumentationRegistry.getArguments()

    @Test
    fun loginYCatalogosContraApiPublica() = runBlocking {
        assumeTrue(
            "Requiere el túnel activo: ejecuta con -e teqrutaLive 1",
            argumentos.getString("teqrutaLive") == "1"
        )

        Log.i(TAG, "BaseUrl embebida: ${ApiConfig.baseUrl}")
        assertTrue(
            "La app debe usar HTTPS público, no el puerto interno",
            ApiConfig.baseUrl.startsWith("https://") && !ApiConfig.baseUrl.contains(":5000")
        )

        val auth = NetworkModule.createAuthService()
        val login = auth.login(LoginRequestWire("tecnico@teqmed.cl", "tecnico123"))
        assertTrue("Login HTTP falló: ${login.code()}", login.isSuccessful)

        val data = login.body()?.data
        assertNotNull("El backend no devolvió datos de sesión", data)
        requireNotNull(data)
        assertTrue("JWT demasiado corto", data.accessToken.length > 20)
        assertTrue("Rol inesperado: ${data.user.rol}", data.user.rol.isNotBlank())

        SessionManager.saveBackendSession(
            role = data.user.rol,
            token = data.accessToken,
            email = data.user.email,
            nombre = data.user.nombre ?: "",
            cargo = data.user.cargo ?: "",
            vehiculoAsignado = data.user.vehiculoAsignado
        )

        val api = NetworkModule.createApi()
        val destinos = api.obtenerDestinos().getOrThrow()
        assertFalse("El catálogo de destinos llegó vacío", destinos.isEmpty())
        assertTrue(destinos.all { it.id.isNotBlank() && it.nombre.isNotBlank() })

        val tarifa = api.obtenerTarifaCombustible().getOrThrow()
        assertTrue("Tarifa inválida: ${tarifa.precioPorLitro}", tarifa.precioPorLitro > 0.0)

        Log.i(
            TAG,
            "OK: ${destinos.size} destinos, tarifa=${tarifa.precioPorLitro} CLP/L (${tarifa.fuente})"
        )
        SessionManager.logout()
    }

    private companion object {
        const val TAG = "TeqRutaPublicApi"
    }
}
