package com.adso.pizca.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// DAO = quien busca y anota en la tabla "perfil"
@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfil WHERE id = 1")
    fun observar(): Flow<PerfilEntity?>       // null = aún no hay perfil

    @Upsert                                   // inserta si no existe, actualiza si ya existe
    suspend fun guardar(perfil: PerfilEntity)
}
