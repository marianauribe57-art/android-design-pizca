package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Receta

/** Lo que la app puede pedir sobre recetas. No dice si vienen de internet o no. */
interface RecetaRepository {
    suspend fun obtenerRecetas(): Result<List<Receta>>                          // Inicio sin filtro
    suspend fun obtenerCategorias(): Result<List<String>>                       // chips de Inicio
    suspend fun obtenerPorCategoria(categoria: String): Result<List<Receta>>    // Inicio con chip
    suspend fun buscarPorNombre(nombre: String): Result<List<Receta>>           // Búsqueda
    suspend fun buscarPorIngredientes(ingredientes: List<String>): Result<List<Receta>>
    suspend fun obtenerPaises(): Result<List<String>>                           // chips de país
    suspend fun buscarPorPais(pais: String): Result<List<Receta>>
    suspend fun obtenerPorId(id: String): Result<Receta>                        // Detalle y Chef
}