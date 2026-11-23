package com.adso.pizca.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// El "cuaderno" con todas las tablas de Pizca
@Database(
    entities = [RecetaFavoritaEntity::class, PerfilEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PizcaDatabase : RoomDatabase() {
    abstract fun favoritaDao(): FavoritaDao
    abstract fun perfilDao(): PerfilDao
}
