package com.adso.pizca.data.repository

import com.adso.pizca.PizcaApp
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.local.FavoritaDao
import com.adso.pizca.data.local.toFavoritaEntity
import com.adso.pizca.data.local.toReceta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// La "cocina" del menú FavoritaRepository: guarda las favoritas en Room
class FavoritaRepositoryImpl(
    private val dao: FavoritaDao = PizcaApp.database.favoritaDao()
) : FavoritaRepository {

    override fun observarFavoritas(): Flow<List<Receta>> =
        dao.observarTodas().map { lista -> lista.map { it.toReceta() } }

    override fun esFavorita(id: String): Flow<Boolean> =
        dao.observarSiExiste(id)

    override suspend fun alternarFavorita(receta: Receta): Result<Unit> = runCatching {
        if (dao.existe(receta.id)) dao.eliminar(receta.id)
        else dao.insertar(receta.toFavoritaEntity())
    }
}
