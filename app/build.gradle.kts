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

// Tarea de diagnóstico: ./gradlew :app:printApiBaseUrl
// La URL se pasa como input para no capturar el script y romper la configuration cache.
tasks.register("printApiBaseUrl") {
    inputs.property("apiBaseUrl", apiBaseUrl)
    doLast {
        println("API_BASE_URL=${inputs.properties["apiBaseUrl"]}")
    }
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
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