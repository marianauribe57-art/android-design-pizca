package com.adso.pizca

import android.app.Application
import androidx.room.Room
import com.adso.pizca.data.local.PizcaDatabase

// Se ejecuta UNA vez cuando arranca la app, antes que cualquier pantalla
class PizcaApp : Application() {

    companion object {
        lateinit var database: PizcaDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, PizcaDatabase::class.java, "pizca.db").build()
    }
}
