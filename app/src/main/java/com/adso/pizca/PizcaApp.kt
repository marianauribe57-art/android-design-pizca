package com.adso.pizca

import android.app.Application
import androidx.room.Room
import com.adso.pizca.data.local.PizcaDatabase
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// Se ejecuta UNA vez cuando arranca la app, antes que cualquier pantalla
class PizcaApp : Application() {

    companion object {
        lateinit var database: PizcaDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, PizcaDatabase::class.java, "pizca.db").build()
        // Firebase + App Check (modo depuración para el emulador)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        // Pide el pase de App Check al arrancar: así el código de depuración sale en Logcat.
        // Se puede quitar cuando todo el equipo tenga el token
        Firebase.appCheck.getAppCheckToken(false)
    }
}
