package com.teqmed.teqruta.di

import android.content.Context
import com.teqmed.teqruta.data.AppDatabase
import com.teqmed.teqruta.data.network.NetworkModule
import com.teqmed.teqruta.data.repository.AuthRepository
import com.teqmed.teqruta.data.repository.RetrofitAuthRepository
import com.teqmed.teqruta.data.repository.ViajeRepository

/** Contenedor manual de dependencias con el backend Spring Boot ya conectado. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context.applicationContext)
    private val api = NetworkModule.createApi()

    val viajeRepository = ViajeRepository(database.viajeDao(), api)
    val authRepository: AuthRepository = RetrofitAuthRepository(NetworkModule.createAuthService())
}
