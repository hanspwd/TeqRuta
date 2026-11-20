package com.teqmed.teqruta

import com.teqmed.teqruta.data.SessionManager
import com.teqmed.teqruta.data.network.AuthService
import com.teqmed.teqruta.data.network.RetrofitTeqRutaApi
import com.teqmed.teqruta.data.network.TeqRutaService
import com.teqmed.teqruta.data.network.dto.ApiEnvelope
import com.teqmed.teqruta.data.network.dto.DestinoWire
import com.teqmed.teqruta.data.network.dto.LoginDataWire
import com.teqmed.teqruta.data.network.dto.LoginRequestWire
import com.teqmed.teqruta.data.network.dto.SyncRechazoWire
import com.teqmed.teqruta.data.network.dto.SyncRequestWire
import com.teqmed.teqruta.data.network.dto.SyncResponseWire
import com.teqmed.teqruta.data.network.dto.SyncViajeWire
import com.teqmed.teqruta.data.network.dto.TarifaWire
import com.teqmed.teqruta.data.network.dto.UserWire
import com.teqmed.teqruta.data.network.dto.ViajeDto
import com.teqmed.teqruta.data.repository.LoginResult
import com.teqmed.teqruta.data.repository.RetrofitAuthRepository
import com.teqmed.teqruta.data.repository.ViajeRepository
import com.teqmed.teqruta.model.Viaje
import com.teqmed.teqruta.model.ViajeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

private class FakeTeqRutaService : TeqRutaService {
    var destinos: Response<ApiEnvelope<List<DestinoWire>>> = Response.success(ApiEnvelope(emptyList()))
    var tarifa: Response<ApiEnvelope<TarifaWire>> = Response.success(
        ApiEnvelope(TarifaWire(precioLitro = 0.0))
    )
    var sync: Response<ApiEnvelope<SyncResponseWire>> = Response.success(ApiEnvelope(SyncResponseWire()))
    var ultimoBody: SyncRequestWire? = null

    override suspend fun obtenerDestinos() = destinos
    override suspend fun obtenerTarifa() = tarifa
    override suspend fun sincronizar(body: SyncRequestWire): Response<ApiEnvelope<SyncResponseWire>> {
        ultimoBody = body
        return sync
    }
}

private class FakeAuthService : AuthService {
    var respuesta: Response<ApiEnvelope<LoginDataWire>> = error401()
    var ultimoRequest: LoginRequestWire? = null

    override suspend fun login(request: LoginRequestWire): Response<ApiEnvelope<LoginDataWire>> {
        ultimoRequest = request
        return respuesta
    }

    companion object {
        fun error401(): Response<ApiEnvelope<LoginDataWire>> {
            val json = "{\"error\":{\"code\":\"invalid_credentials\",\"message\":\"Credenciales inválidas\"}}"
            return Response.error(401, json.toResponseBody("application/json".toMediaType()))
        }
    }
}

private class FakeViajeDaoParaApi : ViajeDao {
    private val viajes = mutableListOf<Viaje>()
    private val flow = MutableStateFlow<List<Viaje>>(emptyList())

    override fun insertViaje(viaje: Viaje) {
        viajes.add(viaje)
        flow.value = viajes.toList()
    }

    override fun getViajesPendientes(): List<Viaje> = viajes.filter { !it.sincronizado }

    override fun marcarComoSincronizados(uuids: List<String>) {
        viajes.forEachIndexed { index, viaje ->
            if (viaje.uuid in uuids) {
                viajes[index] = viaje.copy(sincronizado = true)
            }
        }
        flow.value = viajes.toList()
    }

    override fun actualizarEstadoAprobacion(uuid: String, estado: String) = Unit

    override fun actualizarEstadoAprobacionConObservacion(
        uuid: String,
        estado: String,
        observacion: String
    ) = Unit

    override fun getAllViajes(): Flow<List<Viaje>> = flow

    override fun deleteViajeByUuid(uuid: String) {
        viajes.removeAll { it.uuid == uuid }
        flow.value = viajes.toList()
    }
}

private fun viajeDePruebaApi() = Viaje(
    rutaSeleccionada = "Sede Concepción - Clínica RedSalud Concepción",
    odometroInicio = 1000,
    fotoOdometroInicio = null,
    odometroFin = 1023,
    fotoOdometroFin = null,
    totalMontoPeajes = 500.0,
    fotosPeajes = "",
    horaInicio = 1763500000000L,
    horaFin = 1763503600000L,
    tecnicoEmail = "tecnico@teqmed.cl",
    vehiculoPatente = "HK-8932"
)

private fun viajeDePruebaDto() = ViajeDto(
    uuid = "11111111-1111-4111-8111-111111111111",
    rutaSeleccionada = "Sede Concepción - Clínica RedSalud Concepción",
    odometroInicio = 1000,
    odometroFin = 1023,
    totalMontoPeajes = 500.0,
    horaInicio = 1763500000000L,
    horaFin = 1763503600000L,
    eventosExtra = "",
    sincronizado = false,
    tecnicoEmail = "tecnico@teqmed.cl",
    vehiculoPatente = "HK-8932"
)

private fun sesionDePrueba() = SessionManager.saveBackendSession(
    role = "tecnico",
    token = "token-de-prueba",
    email = "tecnico@teqmed.cl",
    nombre = "Hans Morales",
    cargo = "Técnico de Servicio en Terreno",
    vehiculoAsignado = "HK-8932 (Toyota Hilux)"
)

class ApiIntegrationTest {

    @After
    fun limpiarSesion() {
        SessionManager.logout()
    }

    @Test
    fun retrofitMapeaDestinosYTarifaDelBackend() = runTest {
        val service = FakeTeqRutaService().apply {
            destinos = Response.success(
                ApiEnvelope(
                    listOf(
                        DestinoWire(7L, "Hospital Regional de Chillán", 315.0),
                        DestinoWire(9L, "Clínica RedSalud Concepción", 12.5)
                    )
                )
            )
            tarifa = Response.success(
                ApiEnvelope(
                    TarifaWire(
                        precioLitro = 1581.0,
                        vigenteDesde = "2026-11-19T10:00:00Z",
                        origen = "API_EXTERNA",
                        fuente = "CNE"
                    )
                )
            )
        }
        val api = RetrofitTeqRutaApi(service)

        val destinos = api.obtenerDestinos().getOrThrow()
        val tarifa = api.obtenerTarifaCombustible().getOrThrow()

        assertEquals(2, destinos.size)
        assertEquals("7", destinos[0].id)
        assertEquals("Hospital Regional de Chillán", destinos[0].nombre)
        assertEquals(315.0, destinos[0].distanciaOficialKm, 0.0)
        assertEquals(1581.0, tarifa.precioPorLitro, 0.0)
        assertEquals("CNE", tarifa.fuente)
    }

    @Test
    fun syncAceptaInsertadosYDuplicadosPeroNoRechazados() = runTest {
        val service = FakeTeqRutaService().apply {
            sync = Response.success(
                ApiEnvelope(
                    SyncResponseWire(
                        rechazados = listOf(SyncRechazoWire(1, listOf("odometroFin inválido"))),
                        viajes = listOf(
                            SyncViajeWire("11111111-1111-4111-8111-111111111111", "insertado"),
                            SyncViajeWire("22222222-2222-4222-8222-222222222222", null)
                        )
                    )
                )
            )
        }
        sesionDePrueba()

        val aceptados = RetrofitTeqRutaApi(service)
            .subirViajes(listOf(viajeDePruebaDto()))
            .getOrThrow()

        assertEquals(2, aceptados.size)
        assertEquals("tecnico@teqmed.cl", service.ultimoBody?.viajes?.first()?.tecnicoEmail)
        assertEquals("HK-8932", service.ultimoBody?.viajes?.first()?.vehiculoPatente)
    }

    @Test
    fun repositorioMarcaSoloUuidsAceptados() = runTest {
        val dao = FakeViajeDaoParaApi()
        val service = FakeTeqRutaService().apply {
            sync = Response.success(
                ApiEnvelope(
                    SyncResponseWire(
                        viajes = listOf(SyncViajeWire("uuid-aceptado", "insertado"))
                    )
                )
            )
        }
        sesionDePrueba()
        dao.insertViaje(viajeDePruebaApi().copy(uuid = "uuid-aceptado"))

        val marcados = ViajeRepository(dao, RetrofitTeqRutaApi(service)).sincronizarPendientes()

        assertEquals(1, marcados)
        assertTrue(dao.getViajesPendientes().isEmpty())
    }

    @Test
    fun loginGuardaJwtYPerfilEnSesion() = runTest {
        val service = FakeAuthService().apply {
            respuesta = Response.success(
                ApiEnvelope(
                    LoginDataWire(
                        accessToken = "jwt-firmado",
                        tokenType = "Bearer",
                        expiresIn = 28800,
                        user = UserWire(
                            email = "tecnico@teqmed.cl",
                            nombre = "Hans Morales",
                            rol = "tecnico",
                            cargo = "Técnico de Servicio en Terreno",
                            vehiculoAsignado = "HK-8932"
                        )
                    )
                )
            )
        }

        val resultado = RetrofitAuthRepository(service).login("tecnico@teqmed.cl", "tecnico123")

        assertTrue(resultado is LoginResult.Success && resultado.role == "tecnico")
        assertEquals("jwt-firmado", SessionManager.token)
        assertEquals("tecnico@teqmed.cl", SessionManager.currentUserEmail)
        assertEquals("HK-8932", SessionManager.vehiculoAsignado)
        assertEquals("tecnico123", service.ultimoRequest?.password)
    }

    @Test
    fun loginConCredencialesInvalidasLimpiaSesion() = runTest {
        sesionDePrueba()
        val resultado = RetrofitAuthRepository(FakeAuthService())
            .login("jefe@teqmed.cl", "mala")

        assertTrue(resultado is LoginResult.Error)
        assertTrue((resultado as LoginResult.Error).message.contains("Credenciales inválidas"))
        assertEquals(null, SessionManager.token)
    }
}
