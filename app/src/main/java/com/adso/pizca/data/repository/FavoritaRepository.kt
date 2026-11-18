package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Receta
import kotlinx.coroutines.flow.Flow

/** Las recetas marcadas con el corazón. Flow = se actualiza solo cuando cambia la tabla. */
interface FavoritaRepository {
    fun observarFavoritas(): Flow<List<Receta>>
    fun esFavorita(id: String): Flow<Boolean>
    suspend fun alternarFavorita(receta: Receta): Result<Unit>   // si está la quita, si no la guarda
}