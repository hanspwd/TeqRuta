# Integración con TeqRuta-API

Detalle técnico de cómo la app consume la API Flask publicada por Ngrok.

- API: <https://github.com/Willy-Duoc/TeqRuta-API>
- App: <https://github.com/hanspwd/TeqRuta>

## Requisitos de integración

- Flask escucha en `127.0.0.1:5000` (loopback); Ngrok es la única salida pública HTTPS.
- La app usa la URL pública, nunca `127.0.0.1` ni el puerto interno.
- Credenciales demo creadas por el seed de la API:

| Correo | Password | Rol |
|---|---|---|
| `tecnico@teqmed.cl` | `tecnico123` | tecnico |
| `jefe@teqmed.cl` | `jefe123` | jefe |
| `gerencia@teqmed.cl` | `gerencia123` | gerencia |
| `admin@teqmed.cl` | `admin123` | admin |

## Archivos de integración en la app

| Archivo | Responsabilidad |
|---|---|
| `data/network/NetworkModule.kt` | Retrofit + OkHttp; agrega `Authorization`, `Accept`, `Ngrok-Skip-Browser-Warning` y timeouts. |
| `data/network/ApiConfig.kt` | Normaliza la base URL y define la patente demo por defecto. |
| `data/network/RetrofitTeqRutaApi.kt` | Implementa destinos, tarifa y sincronización. |
| `data/network/AuthService.kt` + `data/network/dto/` | Login y DTOs del contrato `data/message/meta`. |
| `data/network/ApiErrorParser.kt` | Convierte errores HTTP/red en mensajes de UI. |
| `data/repository/RetrofitAuthRepository.kt` | Login, guarda JWT/perfil en `SessionManager`. |
| `data/repository/ViajeRepository.kt` | Envía `tecnicoEmail` y `vehiculoPatente`; marca solo UUID aceptados. |
| `data/SessionManager.kt` | Sesión, token, rol y vehículo asignado. |
| `ui/screens/LoginScreen.kt` | Login real y navegación según el rol devuelto. |
| `viewmodel/ViajeViewModel.kt` | Sincronización manual contra el repositorio HTTP. |
| `TeqRutaApp.kt` + `worker/SyncWorker.kt` | Contenedor de dependencias y sync periódica cada 15 min. |
| `di/AppContainer.kt` | Inyección del repositorio de autenticación real. |

## URL del backend

Prioridad:

```text
-PteqrutaApiBaseUrl  >  TEQRUTA_API_BASE_URL  >  local.properties  >  http://10.0.2.2:5000/
```

`10.0.2.2:5000` es solo el host del emulador para desarrollo local; un teléfono físico
debe usar la URL HTTPS de Ngrok.

Con ambos repositorios hermanos:

```bash
cd ../TeqRuta-API
make setup          # crea .env, .venv e instala Ngrok si falta
# edita .env -> NGROK_AUTHTOKEN=...
make publicar       # Flask loopback + túnel y escribe local.properties
cd ../TeqRuta
./gradlew :app:printApiBaseUrl
```

Resultado esperado:

```text
API_BASE_URL=https://mi-dominio.ngrok-free.dev/
```

Si la app está en otra ruta:

```bash
cd /ruta/TeqRuta-API
TEQRUTA_APP_DIR=/ruta/TeqRuta make publicar
```

El script preserva `sdk.dir` en `local.properties`. Si el archivo no existe, Android
Studio lo completa al sincronizar; para CLI agrégalo manualmente.

### Cuándo recompilar

- `NGROK_DOMAIN` fijo (recomendado): la URL no cambia; el APK sigue funcionando.
- Sin dominio estático: Ngrok puede asignar otra URL en cada inicio; reconstruye el APK.
- Cambios manuales de URL: ejecuta `./gradlew :app:assembleDebug` para reinyectar
  `BuildConfig.API_BASE_URL`.

## Flujo de sincronización

1. Login → se guarda `access_token` en `SessionManager`.
2. El interceptor agrega `Authorization: Bearer <token>`.
3. `SyncWorker` o el ViewModel llaman a `sincronizarPendientes()`.
4. `POST /api/viajes/sync` responde `200` o `207`; se aceptan UUID insertados y
   duplicados. Los rechazados quedan pendientes.
5. Si no hay token, no se envía el lote para evitar `401`.

## Verificación

```bash
# App
cd TeqRuta
./gradlew :app:printApiBaseUrl :app:testDebugUnitTest :app:assembleDebug

# API
cd ../TeqRuta-API
make test        # 30 pruebas
make smoke-local # E2E contra 127.0.0.1:5000
make smoke       # 12 verificaciones E2E contra la URL pública
```

### Prueba instrumentada real

Con túnel activo, emulador/dispositivo conectado y APKs construidos:

```bash
cd TeqRuta
./gradlew :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e teqrutaLive 1 \
  com.teqmed.teqruta.test/androidx.test.runner.AndroidJUnitRunner
```

Sin `-e teqrutaLive 1` la prueba se omite. Valida login, permisos, 5 destinos y tarifa
con el mismo cliente OkHttp/Retrofit de la app.
