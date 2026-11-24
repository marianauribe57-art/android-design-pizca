package com.adso.pizca.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// El "cuaderno" con todas las tablas de Pizca
@Database(
    entities = [RecetaFavoritaEntity::class, PerfilEntity::class, TraduccionEntity::class],  // CAMBIA: + traducciones
    version = 2,                                                                            // CAMBIA: antes 1
    exportSchema = false
)
abstract class PizcaDatabase : RoomDatabase() {
    abstract fun favoritaDao(): FavoritaDao
    abstract fun perfilDao(): PerfilDao
    abstract fun traduccionDao(): TraduccionDao   // NUEVO

    companion object {
        // NUEVO: de la versión 1 a la 2 solo se agrega la tabla "traducciones".
        // Así no se borran las favoritas ni el perfil que ya estaban guardados.
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `traducciones` " +
                        "(`clave` TEXT NOT NULL, `valor` TEXT NOT NULL, PRIMARY KEY(`clave`))"
                )
            }
        }
    }
}
