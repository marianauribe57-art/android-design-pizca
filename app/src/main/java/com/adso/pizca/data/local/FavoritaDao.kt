package com.adso.pizca.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO = quien busca y anota en la tabla "recetas_favoritas"
@Dao
interface FavoritaDao {

    // Flow = "avísame cada vez que la tabla cambie"
    @Query("SELECT * FROM recetas_favoritas ORDER BY nombre")
    fun observarTodas(): Flow<List<RecetaFavoritaEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM recetas_favoritas WHERE id = :id)")
    fun observarSiExiste(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM recetas_favoritas WHERE id = :id)")
    suspend fun existe(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(receta: RecetaFavoritaEntity)

    @Query("DELETE FROM recetas_favoritas WHERE id = :id")
    suspend fun eliminar(id: String)
}
