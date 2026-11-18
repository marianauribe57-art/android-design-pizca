package com.adso.pizca.data.dto

// Un ingrediente. TheMealDB no trae nutrición: 0 = "sin dato" (luego USDA)
data class Ingrediente(
    val nombre: String,
    val medida: String,
    val calorias: Int = 0,
    val proteinas: Int = 0
)

// Una receta completa. Los campos siguen la idea de TheMealDB
data class Receta(
    val id: String,
    val nombre: String,
    val categoria: String = "",
    val pais: String = "",
    val emoji: String = "🍽️",
    val imagenUrl: String = "",                  // NUEVO: la foto (strMealThumb)
    val ingredientes: List<Ingrediente> = emptyList(),
    val pasos: List<String> = emptyList()
)