# TeqRuta — Aplicación Android

App nativa para que técnicos de **TEQMED SpA** registren en terreno los traslados,
odometría, peajes y evidencias fotográficas. Funciona **offline-first** y sincroniza
con la API central cuando hay conexión.

- **Backend oficial:** [TeqRuta-API](https://github.com/Willy-Duoc/TeqRuta-API) (Flask + SQLAlchemy + JWT).
- **Este repositorio:** [TeqRuta](https://github.com/hanspwd/TeqRuta) (Kotlin + Jetpack Compose).
- **Acceso público:** la app consume la API por HTTPS mediante un túnel Ngrok; nunca
  necesita conocer el puerto interno del servidor.

---

## 1. Funcionalidades

- Login real contra la API (`POST /api/auth/login`) con JWT y perfil por rol.
- Rutas por rol: **técnico**, **jefe**, **gerencia** y **admin**.
- Registro de viaje: destino, odómetro inicial/final, peajes y fotos de evidencia.
- Persistencia local con **Room** y cola de viajes pendientes.
- Sincronización idempotente por UUID (`POST /api/viajes/sync`), manual y periódica
  mediante **WorkManager**.
- Historial y estados de aprobación/observación.
- Cálculo de costos y alertas de desviación resueltos en el servidor.
- Captura de cámara y ubicación GPS para respaldar el registro.
- URL del backend configurable sin recompilar código: Gradle, variable de entorno
  o `local.properties`.

## 2. Stack técnico

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 + Navigation Compose |
| Arquitectura | MVVM + contenedor manual de dependencias |
| Persistencia | Room (SQLite) |
| Red | Retrofit 2.11 + OkHttp 4.12 + Gson |
| Sesión | JWT en `SessionManager` |
| Trabajo en segundo plano | WorkManager |
| Build | AGP 9.3, Gradle 9.6, KSP, compileSdk/targetSdk 37, minSdk 26 |

## 3. Estructura del repositorio

```text
TeqRuta/
├── app/
│   ├── src/main/java/com/teqmed/teqruta/
│   │   ├── data/                 # Room, red, repositorios, sesión
│   │   │   └── network/          # Retrofit, DTOs, URL y errores HTTP
│   │   ├── di/                   # Contenedor de dependencias
│   │   ├── model/                # Entidad Viaje y DAO
│   │   ├── ui/                   # Pantallas, navegación, componentes y tema
│   │   ├── utils/                # Red, ubicación, formato y validaciones
│   │   ├── viewmodel/            # Estado y lógica de presentación
│   │   ├── worker/               # Sincronización periódica
│   │   ├── MainActivity.kt
│   │   └── TeqRutaApp.kt
│   ├── src/test/                 # Pruebas JVM
│   ├── src/androidTest/          # Pruebas instrumentadas
│   └── build.gradle.kts
├── docs/                         # Documento de integración y PDFs del caso
├── gradle/                       # Wrapper y catálogo de versiones
├── build.gradle.kts
├── gradle.properties             # Configuración de Gradle y URL del API
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

`local.properties` **no** se versiona: contiene la ruta del SDK y, cuando la API está
publicada, la URL HTTPS del túnel.

## 4. Requisitos

- **Android Studio** reciente (AGP 9.3 / Gradle 9.6) o JDK 17+ para compilar por CLI.
  Gradle puede descargar automáticamente el JDK 25 configurado en el proyecto.
- **Android SDK Platform 37** y **Build-Tools 36** (Android Studio los instala).
- Emulador API 26+ o dispositivo físico Android 8.0+.
- El repositorio [TeqRuta-API](https://github.com/Willy-Duoc/TeqRuta-API) clonado y
  funcionando.
- Conexión a Internet para dependencias y para el túnel Ngrok.

## 5. Inicio rápido (repositorios hermanos)

La forma recomendada es clonar ambos proyectos dentro de la misma carpeta, conservando
los nombres `TeqRuta-API` y `TeqRuta`:

```bash
mkdir teqruta && cd teqruta
git clone https://github.com/Willy-Duoc/TeqRuta-API.git
git clone https://github.com/hanspwd/TeqRuta.git

cd TeqRuta-API
make setup          # .venv + dependencias + Ngrok + .env
# Edita .env y agrega NGROK_AUTHTOKEN (y NGROK_DOMAIN si tienes dominio estático)
make publicar       # Flask en loopback + túnel HTTPS
make smoke          # 12 verificaciones E2E contra la URL pública

cd ../TeqRuta
./gradlew :app:printApiBaseUrl    # confirma la URL inyectada
./gradlew :app:assembleDebug      # genera el APK
```

Abre el proyecto `TeqRuta` en Android Studio, espera la sincronización de Gradle y
ejecuta la app en el emulador o dispositivo. También puedes instalar por CLI:

```bash
./gradlew :app:installDebug
```

**Credenciales demo** (creadas por la API con `SEED_DEMO_USERS=true`):

| Correo | Contraseña | Rol |
|---|---|---|
| `tecnico@teqmed.cl` | `tecnico123` | técnico |
| `jefe@teqmed.cl` | `jefe123` | jefe |
| `gerencia@teqmed.cl` | `gerencia123` | gerencia |
| `admin@teqmed.cl` | `admin123` | admin |

Si los repositorios no quedaron como hermanos, publica indicando la ruta de esta app:

```bash
cd /ruta/TeqRuta-API
TEQRUTA_APP_DIR=/ruta/TeqRuta make publicar
# o: python scripts/tunel.py iniciar --app-dir /ruta/TeqRuta
```

## 6. Configuración de la URL del backend

La URL se resuelve al configurar Gradle, en este orden:

1. `-PteqrutaApiBaseUrl=https://mi-dominio.ngrok-free.dev/`
2. Variable de entorno `TEQRUTA_API_BASE_URL`
3. `local.properties` → `teqrutaApiBaseUrl=...` (lo escribe `make publicar`)
4. Fallback de desarrollo: `http://10.0.2.2:5000/` (API Flask en el host del emulador)

`10.0.2.2` es el alias del host desde el emulador Android; no sirve en un teléfono
físico ni expone el puerto a la red. Para un dispositivo real usa la URL HTTPS de Ngrok.

La URL debe ser **HTTPS** y terminar en `/` (Gradle la normaliza). Comprueba el valor
embebido en el APK con:

```bash
./gradlew :app:printApiBaseUrl
# API_BASE_URL=https://mi-dominio.ngrok-free.dev/
```

`make publicar` actualiza `local.properties` preservando `sdk.dir` y comentarios. Si
Ngrok entrega una URL distinta (plan gratuito sin dominio estático), **recompila el APK**
para reinyectar la nueva URL. Con `NGROK_DOMAIN` configurado en el `.env` de la API, la
URL se mantiene y el APK sigue siendo válido entre reinicios.

El cliente OkHttp agrega automáticamente:

- `Authorization: Bearer <JWT>` cuando hay sesión.
- `Accept: application/json`.
- `Ngrok-Skip-Browser-Warning: 1`, necesario para que el plan gratuito devuelva JSON
  en vez de su página HTML de advertencia.

## 7. Compilar, probar e instalar

```bash
# Pruebas JVM (contrato, repositorios y reglas)
./gradlew :app:testDebugUnitTest

# APK debug
./gradlew :app:assembleDebug
# Salida: app/build/outputs/apk/debug/app-debug.apk

# Instalar en el emulador/dispositivo conectado
./gradlew :app:installDebug
```

En Android Studio: **Open** → selecciona la carpeta `TeqRuta` → espera el *Gradle sync*
→ selecciona un dispositivo → **Run**. Si el SDK no está detectado, crea
`local.properties` con:

```properties
sdk.dir=/ruta/a/Android/Sdk
```

El build **release** aún no está firmado; para la entrega académica se usa el APK debug.

### Prueba instrumentada contra la API pública

Con el túnel activo, un emulador/dispositivo conectado y el APK de pruebas instalado:

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e teqrutaLive 1 \
  com.teqmed.teqruta.test/androidx.test.runner.AndroidJUnitRunner
```

La prueba usa el cliente real de la app para hacer login, leer los destinos y la tarifa.
Sin `-e teqrutaLive 1` se omite, de modo que el repositorio no depende del túnel en CI.

## 8. Backend, contrato y flujo offline-first

La API entrega una envoltura uniforme:

```json
{ "data": {}, "message": "texto", "meta": {} }
```

Puntos principales:

- `POST /api/auth/login` → `access_token` + perfil.
- `GET /api/mantenedores/destinos` → catálogo para operar offline.
- `GET /api/mantenedores/tarifas/vigente` → tarifa de combustible vigente.
- `POST /api/viajes/sync` → lote idempotente por UUID; responde `200`/`207`.
- `GET /api/viajes` → historial filtrado por rol.
- Reportes y mantenedores con RBAC.

Flujo de sincronización:

1. El técnico registra el viaje sin señal; Room lo guarda con `sincronizado = false`.
2. `NetworkHelper` o `SyncWorker` detectan conexión.
3. `ViajeRepository` envía los pendientes con `tecnicoEmail` y `vehiculoPatente`.
4. La API ignora duplicados por UUID y devuelve los aceptados.
5. La app marca como sincronizados únicamente los UUID aceptados.

El contrato completo, endpoints, variables de entorno y despliegue están en el
[README de TeqRuta-API](https://github.com/Willy-Duoc/TeqRuta-API#readme) y en
[`docs/PREPARACION_API.md`](docs/PREPARACION_API.md).

## 9. Pruebas incluidas

| Suite | Comando | Resultado de referencia |
|---|---|---|
| Unitarias Android | `./gradlew :app:testDebugUnitTest` | 10 tests, 0 fallos |
| Instrumentadas Android | ver sección 7 | 2 tests, 0 fallos |
| API (pytest) | `cd ../TeqRuta-API && make test` | 30 tests |
| Smoke E2E público | `cd ../TeqRuta-API && make smoke` | 12/12 OK |

Las unitarias usan servicios falsos y no requieren red. Las instrumentadas y el smoke
validan el túnel HTTPS real.

## 10. Seguridad

- `local.properties` y `.env` están fuera de Git; no subas tokens ni rutas personales.
- El token de Ngrok se guarda solo en el `.env` de la API, con permisos `600`.
- En **debug** se permite HTTP claro únicamente para `10.0.2.2` y `localhost`; el
  manifest **release** no permite tráfico claro.
- La app no conoce ni publica el puerto interno de Flask: solo la URL HTTPS de Ngrok.
- Si un token se filtra, rótalo en el dashboard de Ngrok y actualiza `.env`.
- Para producción real: firma de APK, `SEED_DEMO_USERS=false`, HTTPS propio y
  credenciales fuera del repositorio.

## 11. Solución de problemas

| Síntoma | Causa / solución |
|---|---|
| `API_BASE_URL=http://10.0.2.2:5000/` | La URL pública no se inyectó. Ejecuta `make publicar` en la API y reconstruye. |
| `Cleartext HTTP traffic not permitted` | Estás usando una URL HTTP en release. Usa la URL HTTPS de Ngrok. |
| Ngrok devuelve una página HTML | Falta `Ngrok-Skip-Browser-Warning` (la app ya lo envía) o el túnel no apunta al puerto correcto. |
| `401` al iniciar sesión | Verifica `/api/health`, que la BD tenga seed y las credenciales demo. |
| La app funciona en emulador pero no en físico | El físico necesita la URL pública HTTPS, no `10.0.2.2`. |
| Cambió la URL de Ngrok | Recompila con `make publicar` o configura `NGROK_DOMAIN` estático. |
| `SDK location not found` | Crea `local.properties` con `sdk.dir=...` o abre el proyecto en Android Studio. |
| Gradle no descarga el JDK | Instala JDK 17+ o permite que Gradle/foojay descargue el toolchain 25. |
| No hay dispositivos para `installDebug` | Inicia un emulador o activa depuración USB. |

## 12. Estado y siguientes pasos

MVP funcional y probado contra la API pública. Pendientes:

1. Firma del APK release y keystore fuera del repositorio.
2. Pipeline CI para unitarias + build.
3. Dashboard web administrativo.
4. Migración a PostgreSQL y despliegue con Gunicorn/Nginx para producción.

## 13. Contexto académico

Proyecto de **Desarrollo de Aplicaciones Móviles (DSY1105)**, Escuela de Informática y
Telecomunicaciones, Duoc UC, para TEQMED SpA.

**App Android:** Hans Morales, Martina Norambuena, Williams Contreras.

**API:** Williams Contreras.

> Uso académico y demostrativo. El uso comercial de este software está restringido.
