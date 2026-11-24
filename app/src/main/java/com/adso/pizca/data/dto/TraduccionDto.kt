package com.adso.pizca.data.dto

import kotlinx.serialization.Serializable

// La forma del JSON que nos devuelve Gemini al traducir una receta completa
@Serializable
data class TraduccionDto(
    val nombre: String = "",                 // NUEVO: el nombre del plato también se traduce
    val pasos: List<String> = emptyList(),
    val ingredientes: List<IngredienteTraducidoDto> = emptyList()
)

@Serializable
data class IngredienteTraducidoDto(
    val nombre: String = "",
    val medida: String = ""
)

// NUEVO: varios nombres de recetas traducidos de una sola vez (Inicio y Búsqueda)
@Serializable
data class NombresDto(
    val nombres: List<String> = emptyList()
)

// NUEVO: lo que el usuario busca, pasado al inglés (TheMealDB solo entiende inglés)
@Serializable
data class InglesDto(
    val ingles: String = ""
)
