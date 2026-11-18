package com.adso.pizca.data.api

import com.adso.pizca.data.dto.AreasResponse
import com.adso.pizca.data.dto.CategoriasResponse
import com.adso.pizca.data.dto.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Los endpoints de TheMealDB que usa Pizca (todos GET, solo lectura). */
interface TheMealDbApi {

    @GET("search.php")
    suspend fun buscarPorNombre(@Query("s") nombre: String): MealsResponse

    @GET("lookup.php")
    suspend fun obtenerPorId(@Query("i") id: String): MealsResponse

    @GET("categories.php")
    suspend fun obtenerCategorias(): CategoriasResponse

    @GET("filter.php")
    suspend fun filtrarPorCategoria(@Query("c") categoria: String): MealsResponse

    @GET("filter.php")
    suspend fun filtrarPorIngrediente(@Query("i") ingrediente: String): MealsResponse

    @GET("filter.php")
    suspend fun filtrarPorPais(@Query("a") pais: String): MealsResponse

    @GET("list.php?a=list")
    suspend fun listarPaises(): AreasResponse
}