package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Receta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// TEMPORAL: datos de mentira mientras llegan los repositorios reales. Se borra en la Fase 2.

class FakeRecetaRepository : RecetaRepository {

    private val todas = RecetasDeEjemplo.obtenerRecetas()

    override suspend fun obtenerRecetas(): Result<List<Receta>> {
        delay(800)                                   // simula internet lento
        return Result.success(todas)
    }

    override suspend fun obtenerCategorias(): Result<List<String>> =
        Result.success(todas.map { it.categoria }.distinct())

    override suspend fun obtenerPorCategoria(categoria: String): Result<List<Receta>> {
        delay(500)
        return Result.success(todas.filter { it.categoria == categoria })
    }

    override suspend fun buscarPorNombre(nombre: String): Result<List<Receta>> =
        Result.success(todas.filter { it.nombre.contains(nombre, ignoreCase = true) })

    override suspend fun buscarPorIngredientes(ingredientes: List<String>): Result<List<Receta>> =
        Result.success(todas.filter { receta ->
            ingredientes.all { buscado -> receta.ingredientes.any { it.nombre.contains(buscado, ignoreCase = true) } }
        })

    override suspend fun obtenerPaises(): Result<List<String>> =
        Result.success(todas.map { it.pais }.distinct())

    override suspend fun buscarPorPais(pais: String): Result<List<Receta>> =
        Result.success(todas.filter { it.pais == pais })

    override suspend fun obtenerPorId(id: String): Result<Receta> = runCatching {
        todas.first { it.id == id }
    }
}

class FakeFavoritaRepository : FavoritaRepository {

    private val favoritas = MutableStateFlow<List<Receta>>(emptyList())

    override fun observarFavoritas(): Flow<List<Receta>> = favoritas

    override fun esFavorita(id: String): Flow<Boolean> =
        favoritas.map { lista -> lista.any { it.id == id } }

    override suspend fun alternarFavorita(receta: Receta): Result<Unit> {
        val actual = favoritas.value
        favoritas.value =
            if (actual.any { it.id == receta.id }) actual.filter { it.id != receta.id }
            else actual + receta
        return Result.success(Unit)
    }
}