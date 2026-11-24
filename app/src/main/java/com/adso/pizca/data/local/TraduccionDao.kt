package com.adso.pizca.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// DAO = quien busca y anota en la tabla "traducciones"
@Dao
interface TraduccionDao {

    // null = todavía no la hemos traducido
    @Query("SELECT valor FROM traducciones WHERE clave = :clave")
    suspend fun obtener(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(traduccion: TraduccionEntity)
}
