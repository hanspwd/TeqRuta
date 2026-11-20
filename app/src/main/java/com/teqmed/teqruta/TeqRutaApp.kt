package com.teqmed.teqruta

import android.app.Application
import com.teqmed.teqruta.di.AppContainer
import com.teqmed.teqruta.worker.SyncWorker

class TeqRutaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SyncWorker.enqueue(this)
    }
}
