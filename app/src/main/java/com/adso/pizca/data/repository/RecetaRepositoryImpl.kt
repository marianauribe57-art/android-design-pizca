package com.adso.pizca.data.repository

import com.adso.pizca.data.api.TheMealDbApi
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.mapper.toReceta
import com.adso.pizca.data.network.ApiClient

class RecetaRepositoryImpl(
    private val api: TheMealDbApi = ApiClient.mealDbApi
) : RecetaRepository {

    // Inicio sin filtro: search.php?s= vacío trae varias recetas completas
    override suspend fun obtenerRecetas(): Result<List<Receta>> = runCatching {
        api.buscarPorNombre("").meals.orEmpty().map { it.toReceta() }
    }

    override suspend fun obtenerCategorias(): Result<List<String>> = runCatching {
        api.obtenerCategorias().categories.orEmpty().map { it.strCategory }
    }

    override suspend fun obtenerPorCategoria(categoria: String): Result<List<Receta>> = runCatching {
        api.filtrarPorCategoria(categoria).meals.orEmpty()
            .map { it.toReceta(categoriaPorDefecto = categoria) }
    }

    override suspend fun buscarPorNombre(nombre: String): Result<List<Receta>> = runCatching {
        api.buscarPorNombre(nombre.trim()).meals.orEmpty().map { it.toReceta() }
    }

    // La API gratis filtra por UN ingrediente. Pedimos uno por uno
    // y dejamos solo las recetas que aparecen en TODAS las respuestas.
    override suspend fun buscarPorIngredientes(ingredientes: List<String>): Result<List<Receta>> = runCatching {
        if (ingredientes.isEmpty()) return@runCatching emptyList<Receta>()
        val listas = ingredientes.map { ingrediente ->
            val formato = ingrediente.trim().lowercase().replace(" ", "_")   // "chicken breast" → "chicken_breast"
            api.filtrarPorIngrediente(formato).meals.orEmpty().map { it.toReceta() }
        }
        val idsEnTodas = listas
            .map { lista -> lista.map { it.id }.toSet() }
            .reduce { a, b -> a intersect b }
        listas.first().filter { it.id in idsEnTodas }
    }

    override suspend fun obtenerPaises(): Result<List<String>> = runCatching {
        api.listarPaises().meals.orEmpty().map { it.strArea }
    }

    override suspend fun buscarPorPais(pais: String): Result<List<Receta>> = runCatching {
        api.filtrarPorPais(pais).meals.orEmpty().map { it.toReceta().copy(pais = pais) }
    }

    override suspend fun obtenerPorId(id: String): Result<Receta> = runCatching {
        api.obtenerPorId(id).meals?.firstOrNull()?.toReceta()
            ?: error("No encontramos la receta $id")   // error() lanza una excepción → Result.failure
    }
}