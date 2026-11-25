import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.6"
}

// Prioridad: -PteqrutaApiBaseUrl > TEQRUTA_API_BASE_URL > local.properties > 10.0.2.2:5000.
// `scripts/tunel.py` del proyecto TeqRuta-API escribe local.properties con la URL
// HTTPS pública de Ngrok; así la app nunca necesita conocer el puerto interno.
val localPropertiesText = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties")
).asText.orNull

val localProperties = Properties().apply {
    localPropertiesText?.let { load(it.reader()) }
}

val envApiBaseUrl = providers.environmentVariable("TEQRUTA_API_BASE_URL").orNull

val apiBaseUrl: String = (
    project.findProperty("teqrutaApiBaseUrl") as String?
        ?: envApiBaseUrl
        ?: localProperties.getProperty("teqrutaApiBaseUrl")
        ?: "http://10.0.2.2:5000/"
).trim().let { if (it.endsWith("/")) it else "$it/" }

require(apiBaseUrl.startsWith("http://") || apiBaseUrl.startsWith("https://")) {
    "teqrutaApiBaseUrl debe empezar con http:// o https:// (valor actual: $apiBaseUrl)"
}

// DSN de Sentry para el reporte de errores en producción. Es opcional: si queda
// vacío, el SDK no se inicializa (ver TeqRutaApp). Prioridad: Gradle (-P), variable
// de entorno, local.properties o vacío.
val sentryDsn: String = (
    project.findProperty("teqrutaSentryDsn") as String?
        ?: providers.environmentVariable("TEQRUTA_SENTRY_DSN").orNull
        ?: localProperties.getProperty("teqrutaSentryDsn")
        ?: ""
).trim()

// Tarea de diagnóstico: ./gradlew :app:printApiBaseUrl
// La URL se pasa como input para no capturar el script y romper la configuration cache.
tasks.register("printApiBaseUrl") {
    inputs.property("apiBaseUrl", apiBaseUrl)
    doLast {
        println("API_BASE_URL=${inputs.properties["apiBaseUrl"]}")
    }
}

// ---------------------------------------------------------------------------
// Firma de release (el keystore SIEMPRE vive fuera del repositorio)
// ---------------------------------------------------------------------------
// Prioridad para localizar credenciales:
//   1. Variables de entorno (CI): TEQRUTA_KEYSTORE_PROPERTIES apunta al archivo
//      .properties; TEQRUTA_KEYSTORE_FILE/_PASSWORD/_ALIAS/_KEY_PASSWORD sobrescriben campos.
//   2. ~/.config/teqruta/keystore.properties (lo crea scripts/generar_keystore.sh).
// Si no hay credenciales, el APK release se genera sin firmar y el build no falla.
val keystorePropertiesPath: String? =
    providers.environmentVariable("TEQRUTA_KEYSTORE_PROPERTIES").orNull
        ?: File(System.getProperty("user.home"), ".config/teqruta/keystore.properties")
            .takeIf { it.isFile }?.absolutePath

val keystoreProperties: Properties? = keystorePropertiesPath
    ?.takeIf { File(it).isFile }
    ?.let { ruta -> Properties().apply { File(ruta).inputStream().use { load(it) } } }

val keystorePropertiesDir: File? = keystorePropertiesPath?.let { File(it).parentFile }

fun credencial(nombre: String, env: String): String? =
    providers.environmentVariable(env).orNull ?: keystoreProperties?.getProperty(nombre)

val releaseStoreFile: File? = credencial("storeFile", "TEQRUTA_KEYSTORE_FILE")?.let { valor ->
    val archivo = File(valor)
    (if (archivo.isAbsolute) archivo else File(keystorePropertiesDir ?: rootDir, valor))
        .takeIf { it.isFile }
}
val releaseStorePassword = credencial("storePassword", "TEQRUTA_KEYSTORE_PASSWORD")
val releaseKeyAlias = credencial("keyAlias", "TEQRUTA_KEY_ALIAS")
val releaseKeyPassword = credencial("keyPassword", "TEQRUTA_KEY_PASSWORD")

val firmaReleaseConfigurada = releaseStoreFile != null &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

if (!firmaReleaseConfigurada) {
    logger.lifecycle(
        "[TeqRuta] Firma release no configurada: el APK release quedará SIN FIRMAR. " +
            "Ejecuta scripts/generar_keystore.sh para habilitarla."
    )
}

android {
    namespace = "com.teqmed.teqruta"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.teqmed.teqruta"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // URL de la API TeqRuta; se inyecta en BuildConfig para Retrofit.
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        // DSN opcional de Sentry (reporte de errores en producción).
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (firmaReleaseConfigurada) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (firmaReleaseConfigurada) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Cliente HTTP contra la API TeqRuta.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // Reporte de errores en producción; se activa solo si BuildConfig.SENTRY_DSN tiene valor.
    implementation(libs.sentry.android)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
}