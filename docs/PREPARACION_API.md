# Integración con la API TeqRuta

Estado: **la app YA está conectada al microservicio Spring Boot**. Ya no usa el stub
en ejecución; el stub se conserva únicamente como doble de pruebas unitarias.

## Backend

- Proyecto: `C:\Users\Mango\Desktop\Proyectos\TeqRuta-Backend`
- Contrato y variables de entorno: `TeqRuta-Backend\README.md`
- Ejecución local: `.\gradlew.bat bootRun` (puerto 8080 por defecto).

## Credenciales reales (seed del backend)

| Correo | Password | Rol |
|---|---|---|
| `tecnico@teqmed.cl` | `tecnico123` | tecnico |
| `jefe@teqmed.cl` | `jefe123` | jefe |
| `gerencia@teqmed.cl` | `gerencia123` | gerencia |
| `admin@teqmed.cl` | `admin123` | admin |

Ya no sirven los correos `@teqruta.cl` del stub.

## Cambios realizados en la app

| Archivo | Cambio |
|---|---|
| `data/network/NetworkModule.kt` | Cliente Retrofit + OkHttp con interceptor de `Authorization: Bearer`, `Accept`, `Ngrok-Skip-Browser-Warning` y timeouts. |
| `data/network/ApiConfig.kt` | Normaliza la base URL (siempre termina en `/`) y define la patente demo por defecto. |
| `data/network/RetrofitTeqRutaApi.kt` | Implementación real de `TeqRutaApi`: destinos, tarifa y `POST /api/viajes/sync`. |
| `data/network/AuthService.kt` + `data/network/dto/ApiWireDtos.kt` | Servicio y DTOs del contrato JSON (envoltura `data/message/meta`). |
| `data/network/ApiErrorParser.kt` | Traduce errores HTTP/red a mensajes de UI. |
| `data/repository/RetrofitAuthRepository.kt` | `POST /api/auth/login`, guarda JWT y perfil en `SessionManager`. |
| `data/repository/ViajeRepository.kt` | Envía `tecnicoEmail` y `vehiculoPatente`; marca como sincronizados solo los UUID aceptados. |
| `data/SessionManager.kt` | Conserva los perfiles demo de la UI y guarda `token`, `email`, `rol` y `vehiculoAsignado` del backend. |
| `ui/screens/LoginScreen.kt` | Llama a `POST /api/auth/login` de forma real; muestra el error del servidor y navega según el rol devuelto. |
| `ui/screens/AppNavigation.kt` | Usa el `SessionManager` unificado y cierra la sesión limpiando el JWT. |
| `viewmodel/ViajeViewModel.kt` | `sincronizarPendientes()` usa el repositorio HTTP; los viajes guardados llevan el email/patente de la sesión. |
| `TeqRutaApp.kt` + `worker/SyncWorker.kt` | Contenedor de dependencias y sincronización periódica cada 15 min con red. |
| `data/network/dto/Dtos.kt` | `ViajeDto` ampliado con los campos exigidos por el backend. |
| `di/AppContainer.kt` | Inyecta el repositorio de auth real. |
| `gradle.properties` / `app/build.gradle.kts` | Configuración de `teqrutaApiBaseUrl` ya activa y dependencias Retrofit/OkHttp/WorkManager. |

La UI y Room se conservan del estado remoto; solo se conectan los puntos de entrada a la API.

## Flujo de sincronización

1. Login → `data.access_token` se guarda en `SessionManager`.
2. El interceptor agrega `Authorization: Bearer <token>` a cada petición.
3. `SyncWorker` llama a `sincronizarPendientes()` cada 15 min con red.
4. `POST /api/viajes/sync` responde 200 o 207; se aceptan los UUID de `data.viajes`
   (insertados **y** duplicados). Los de `data.rechazados` quedan pendientes.
5. Si no hay token, no se envía el lote (evita 401 innecesarios).

Si el usuario autenticado no tiene patente asignada se usa `HK-8932` (valor del seed demo).

## Configurar la URL

En `gradle.properties`:

```properties
# Emulador
teqrutaApiBaseUrl=http://10.0.2.2:8080/
# Dispositivo físico: IP de la máquina que corre el backend
# Ngrok free (el interceptor ya agrega la cabecera requerida):
# teqrutaApiBaseUrl=https://xxxx.ngrok-free.app/
```

Debe terminar en `/`. El debug manifest permite HTTP claro; release debe usar HTTPS.

## Verificación

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Resultado: `BUILD SUCCESSFUL` con **10 pruebas unitarias** (5 de reglas de negocio + 5 de integración con servicios falsos).

Smoke HTTP real contra el backend: login → 5 destinos → tarifa 1597 CLP →
`POST /api/viajes/sync` HTTP 200 con 1 viaje insertado.
