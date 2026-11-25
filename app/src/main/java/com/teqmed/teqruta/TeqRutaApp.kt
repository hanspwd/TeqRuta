package com.teqmed.teqruta

import android.app.Application
import com.teqmed.teqruta.di.AppContainer
import com.teqmed.teqruta.worker.SyncWorker
import io.sentry.android.core.SentryAndroid

class TeqRutaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        inicializarSentry()
        container = AppContainer(this)
        SyncWorker.enqueue(this)
    }

    /**
     * Activa Sentry solo cuando hay un DSN configurado (BuildConfig.SENTRY_DSN).
     * Sin DSN no se inicializa nada, de modo que el reporte de errores es opcional.
     */
    private fun inicializarSentry() {
        if (BuildConfig.SENTRY_DSN.isBlank()) return
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
            options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}"
            options.tracesSampleRate = 0.0
            options.isSendDefaultPii = false
            options.isEnableAutoSessionTracking = true
        }
    }
}
